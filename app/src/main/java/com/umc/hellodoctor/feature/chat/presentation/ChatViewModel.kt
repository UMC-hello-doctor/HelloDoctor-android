package com.umc.hellodoctor.feature.chat.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.hellodoctor.feature.chat.domain.model.ChatSession
import com.umc.hellodoctor.feature.chat.domain.model.Message
import com.umc.hellodoctor.feature.chat.domain.model.MessageType
import com.umc.hellodoctor.feature.chat.domain.repository.ChatDataManager
import com.umc.hellodoctor.feature.chat.domain.repository.ChatRepository
import com.umc.hellodoctor.feature.chat.domain.repository.DepartmentRepository
import com.umc.hellodoctor.feature.chat.data.ai.DepartmentRecommendation
import com.umc.hellodoctor.feature.chat.data.ai.SymptomSummaryResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatDataManager: ChatDataManager,
    private val departmentRepository: DepartmentRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val TAG = "ChatViewModel"
    // 현재 채팅 세션
    private val _chatSession = MutableLiveData<ChatSession>()
    val chatSession: LiveData<ChatSession> = _chatSession

    // 메시지 리스트
    private val _messages = MutableLiveData<MutableList<Message>>()
    val messages: LiveData<MutableList<Message>> = _messages

    // 로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 에러 메시지
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // 현재 추천 진료과 (진료과별 질문 플로우용)
    private val _currentDepartment = MutableLiveData<String>()
    val currentDepartment: LiveData<String> = _currentDepartment

    // 진료과별 질문 목록 (P, Q, R, S, T, A)
    private val _departmentQuestions = MutableLiveData<Map<String, String>>()
    val departmentQuestions: LiveData<Map<String, String>> = _departmentQuestions

    // 현재 질문 단계 (P, Q, R, S, T, A 중 하나)
    private val _currentQuestionStage = MutableLiveData<String>()
    val currentQuestionStage: LiveData<String> = _currentQuestionStage

    // PQRST 완료 여부
    private val _pqrstComplete = MutableLiveData<Boolean>()
    val pqrstComplete: LiveData<Boolean> = _pqrstComplete

    // 증상 요약 (메시지와 별도 저장)
    private val _symptomSummaryResponse = MutableLiveData<SymptomSummaryResponse?>()
    val symptomSummaryResponse: LiveData<SymptomSummaryResponse?> = _symptomSummaryResponse

    // Gemini 동적 질문 리스트 (nextQuestions)
    private var dynamicQuestions: List<String> = emptyList()

    init {
        initializeSession()
    }

    /**
     * 채팅 세션 초기화
     */
    private fun initializeSession() {
        val newSession = ChatSession(
            id = UUID.randomUUID().toString()
        )
        _chatSession.value = newSession
        _messages.value = mutableListOf()

        // 모든 상태 초기화
        _currentDepartment.value = ""
        _departmentQuestions.value = emptyMap()
        _currentQuestionStage.value = ""
        _pqrstComplete.value = false
        _symptomSummaryResponse.value = null
        _isLoading.value = false
        _errorMessage.value = ""
        dynamicQuestions = emptyList()

        Log.d(TAG, "세션 초기화 완료: ${newSession.id}")
    }


    /**
     * 사용자 답변 추가 (진료과 질문 답변용)
     */
    fun addUserAnswer(answer: String) {
        addMessage(answer, isBot = false, type = MessageType.ANSWER, saveToAnswers = true)
    }

    /**
     * 봇 질문 추가
     */
    fun addBotQuestion(question: String) {
        addMessage(question, isBot = true, type = MessageType.QUESTION, saveToQuestions = true)
    }


    /**
     * 봇 일반 메시지 추가 (세션에 저장 안 함)
     */
    fun addBotMessage(message: String) {
        addMessage(message, isBot = true, type = MessageType.ANSWER)
    }

    /**
     * 응급 경고 메시지 추가 (붉은색 텍스트)
     */
    fun addEmergencyMessage(message: String) {
        addMessage(message, isBot = true, type = MessageType.EMERGENCY, isEmergency = true)
    }

    /**
     * 추천 진료과 추가
     */
    fun addRecommendedDepartment(department: String) {
        addMessage(
            "🏥 추천 진료과: $department",
            isBot = true,
            type = MessageType.RECOMMENDATION
        )
        _chatSession.value?.addRecommendedDepartment(department)
    }

    /**
     * 통합 메시지 추가 메서드 (내부용)
     */
    private fun addMessage(
        text: String,
        isBot: Boolean,
        type: MessageType,
        isEmergency: Boolean = false,
        saveToQuestions: Boolean = false,
        saveToAnswers: Boolean = false
    ) {
        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
        val message = Message(
            text = text,
            isBot = isBot,
            type = type,
            isEmergency = isEmergency
        )
        currentMessages.add(message)
        _messages.value = currentMessages

        // 세션에 저장
        if (saveToQuestions) {
            _chatSession.value?.questions?.add(text)
            Log.d(TAG, "질문 저장: $text")
        }
        if (saveToAnswers) {
            _chatSession.value?.answers?.add(text)
            Log.d(TAG, "답변 저장: $text")
        }
        if (isEmergency) {
            Log.w(TAG, "⚠️ 응급 메시지: $text")
        }
    }

    /**
     * 추천 진료과 자동화 및 진료과별 질문 플로우 시작
     */
    fun recommendAndStartDepartmentFlow() {
        val session = _chatSession.value ?: return
        val symptoms = session.answers.toList()
        Log.d(TAG, "recommendAndStartDepartmentFlow: $symptoms")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val detailed: DepartmentRecommendation? = withContext(Dispatchers.IO) {
                    chatDataManager.recommendDepartmentDetailed(symptoms)
                }

                if (detailed != null && detailed.department.isNotBlank()) {
                    addRecommendedDepartment(detailed.department)
                    if (detailed.reason.isNotBlank()) {
                        addBotMessage(detailed.reason)
                    }
                    if (detailed.isEmergency) {
                        val emergencyMessage = detailed.emergencyReason ?: "즉시 진료가 필요합니다."
                        addEmergencyMessage("⚠️ 응급 증상 가능성: $emergencyMessage")
                    }

                    // AI의 동적 질문 저장
                    if (detailed.nextQuestions?.isNotEmpty() == true) {
                        dynamicQuestions = detailed.nextQuestions
                        Log.d(TAG, "동적 질문 저장: $dynamicQuestions")
                    }

                    startDepartmentQuestionsFlow(detailed.department)
                    Log.d(TAG, "recommendAndStartDepartmentFlow: ${detailed.department}")
                    return@launch
                }

                val recommendedDepts = withContext(Dispatchers.IO) {
                    chatDataManager.recommendDepartments(symptoms)
                }

                if (recommendedDepts.isNotEmpty()) {
                    val selectedDept = recommendedDepts.first()
                    startDepartmentQuestionsFlow(selectedDept)
                    Log.d(TAG, "recommendAndStartDepartmentFlow: $selectedDept")
                    recommendedDepts.forEach { dept ->
                        addRecommendedDepartment(dept)
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "추천 중 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 진료과별 질문 플로우 시작
     * 동적 질문(AI nextQuestions)이 있으면 사용, 없으면 XML 질문 사용
     * @param departmentName 진료과명 (예: "내과 - 소화기")
     */
    fun startDepartmentQuestionsFlow(departmentName: String) {
        _currentDepartment.value = departmentName

        val questions: Map<String, String> = if (dynamicQuestions.isNotEmpty()) {
            // AI의 동적 질문 사용
            Log.d(TAG, "동적 질문 사용: $dynamicQuestions")
            convertDynamicQuestionsToMap(dynamicQuestions)
        } else {
            // XML의 정적 질문 사용
            val deptKey = departmentRepository.getDepartmentKey(departmentName)
            if (deptKey.isEmpty()) {
                _errorMessage.value = "진료과 정보를 찾을 수 없습니다."
                Log.e(TAG, "진료과 키를 찾을 수 없음: $departmentName")
                return
            }

            Log.d(TAG, "정적 XML 질문 사용: $deptKey")
            departmentRepository.getDepartmentQuestions(deptKey)
        }

        if (questions.isEmpty()) {
            _errorMessage.value = "질문을 불러올 수 없습니다."
            Log.e(TAG, "질문 맵이 비어있음")
            return
        }

        _departmentQuestions.value = questions

        // 첫 번째 질문부터 시작
        val firstKey = questions.keys.firstOrNull()
        if (firstKey != null) {
            showNextDepartmentQuestion(firstKey)  // ✅ 여기서 세션에 저장됨
        }
    }

    /**
     * AI의 동적 질문 리스트를 맵 형태로 변환
     * @param dynamicQuestions AI가 생성한 질문 리스트
     * @return 인덱스를 키로 하는 질문 맵
     */
    private fun convertDynamicQuestionsToMap(dynamicQuestions: List<String>): Map<String, String> {
        return dynamicQuestions.mapIndexed { index, question ->
            ("Q${index + 1}") to question
        }.toMap()
    }

    /**
     * 다음 진료과 질문 표시
     * @param stage P, Q, R, S, T, A 중 하나 (또는 Q1, Q2, Q3...)
     */
    fun showNextDepartmentQuestion(stage: String) {
        val questions = _departmentQuestions.value ?: return
        val question = questions[stage] ?: return

        _currentQuestionStage.value = stage

        // ✅ 세션에 질문 저장
        _chatSession.value?.questions?.add(question)
        Log.d(TAG, "질문 세션에 저장: $question")

        val botMessage = Message(
            text = question,
            isBot = true,
            type = MessageType.QUESTION
        )

        val currentMessages = _messages.value ?: mutableListOf()
        currentMessages.add(botMessage)
        _messages.value = currentMessages
    }

    /**
     * PQRST 질문의 다음 단계 진행
     */
    fun proceedToNextQuestionStage() {
        val currentStage = _currentQuestionStage.value ?: return
        val questions = _departmentQuestions.value ?: return

        // 다음 질문 키 찾기
        val questionKeys = questions.keys.toList()
        val currentIndex = questionKeys.indexOf(currentStage)
        if (currentIndex < 0) return

        val nextStageKey = questionKeys.getOrNull(currentIndex + 1)

        if (nextStageKey == null) {
            // 모든 질문 완료
            _pqrstComplete.value = true
            val completionMessage = if (dynamicQuestions.isNotEmpty()) {
                "모든 질문이 완료되었습니다."
            } else {
                "PQRST 완료. 레드플래그 증상을 확인하겠습니다."
            }
            addBotMessage(completionMessage)
            Log.d(TAG, "질문 플로우 완료 (동적: ${dynamicQuestions.isNotEmpty()})")
        } else {
            showNextDepartmentQuestion(nextStageKey)
            Log.d(TAG, "다음 질문으로 진행: $nextStageKey")
        }
    }

    /**
     * 증상 요약 생성 및 추가 (Gemini API 호출)
     */
    fun generateAndAddSymptomSummary() {
        val session = _chatSession.value ?: run {
            Log.e(TAG, "증상 요약 생성 실패: 세션이 null입니다")
            return
        }

        Log.d(TAG, "========== 증상 요약 생성 시작 ==========")
        Log.d(TAG, "질문 개수: ${session.questions.size}")
        Log.d(TAG, "답변 개수: ${session.answers.size}")
        session.questions.forEachIndexed { index, question ->
            Log.d(TAG, "질문[$index]: $question")
        }
        session.answers.forEachIndexed { index, answer ->
            Log.d(TAG, "답변[$index]: $answer")
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "AI API 호출 중...")
                // AI API를 통한 증상 요약 생성
                val summary = withContext(Dispatchers.IO) {
                    chatDataManager.generateSymptomSummaryWithAI(
                        questions = session.questions.toList(),
                        answers = session.answers.toList()
                    )
                }

                Log.d(TAG, "AI 증상 요약 생성 성공")
                Log.d(TAG, "생성된 요약: $summary")

                // 세션 및 ViewModel 업데이트
                session.updateSymptomSummaryResponse(summary)
                _symptomSummaryResponse.value = summary

                Log.d(TAG, "증상 요약 저장 완료")

                // 증상 요약 생성 완료 시 세션을 DB에 저장
                saveChatSession()
            } catch (e: Exception) {
                _errorMessage.value = "증상 요약 생성 중 오류가 발생했습니다."
                Log.e(TAG, "증상 요약 생성 실패: ${e.message}", e)
                Log.e(TAG, "StackTrace: ${e.stackTraceToString()}")
            } finally {
                _isLoading.value = false
                Log.d(TAG, "========== 증상 요약 생성 종료 ==========")
            }
        }
    }

    /**
     * 세션 초기화 (새로운 채팅 시작)
     */
    fun resetSession() {
        initializeSession()
    }

    /**
     * 현재 세션을 DB에 저장
     */
    private fun saveChatSession() {
        val session = _chatSession.value ?: run {
            Log.e(TAG, "세션 저장 실패: 세션이 null입니다")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "========== 세션 DB 저장 시작 ==========")
                chatRepository.saveChatSession(session)
                Log.d(TAG, "세션 저장 완료: ${session.id}")
                Log.d(TAG, "========== 세션 DB 저장 완료 ==========")
            } catch (e: Exception) {
                Log.e(TAG, "세션 저장 실패: ${e.message}", e)
            }
        }
    }

    /**
     * 세션 ID로 저장된 세션 로드
     */
    fun loadSessionById(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = chatRepository.getChatSession(sessionId)
            withContext(Dispatchers.Main) {
                if (session != null) {
                    _chatSession.value = session
                    _symptomSummaryResponse.value = session.symptomSummaryResponse
                } else {
                    _errorMessage.value = "세션을 찾을 수 없습니다."
                }
            }
        }
    }
}
