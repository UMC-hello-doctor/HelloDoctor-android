package com.umc.hellodoctor.feature.auth.domain.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.umc.hellodoctor.BuildConfig
import com.umc.hellodoctor.feature.auth.domain.model.SocialProvider
import com.umc.hellodoctor.feature.auth.domain.model.SocialSignInResult
import com.umc.hellodoctor.feature.auth.domain.model.SocialUser

class GoogleAuthServiceImpl(
    private val context: Context
) : SocialAuthService {

    private val TAG = this.javaClass.simpleName
    private val credentialManager = CredentialManager.create(context)

    override suspend fun signIn(): SocialSignInResult {
        return try {
            // 1차: 이미 권한 준 계정만 대상으로 시도
            signInInternal(filterByAuthorizedAccounts = true)
        } catch (e: NoCredentialException) {
            // 2차: 모든 계정 대상으로 계정 선택/로그인 창 띄우기
            try {
                Log.w(TAG, "NoCredentialException on first attempt, retry with all accounts", e)
                signInInternal(filterByAuthorizedAccounts = false)
            } catch (e2: NoCredentialException) {
                Log.w(TAG, "NoCredentialException on second attempt, no usable Google accounts", e2)
                // 진짜로 사용할 계정이 전혀 없는 상황
                SocialSignInResult.Canceled
            } catch (e2: Exception) {
                Log.e(TAG, "Google sign-in failed on second attempt", e2)
                SocialSignInResult.Error(e2)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed on first attempt", e)
            SocialSignInResult.Error(e)
        }
    }

    private suspend fun signInInternal(
        filterByAuthorizedAccounts: Boolean
    ): SocialSignInResult {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_OAUTH_CLIENT_ID)
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val c = GoogleIdTokenCredential.createFrom(credential.data)

            if (BuildConfig.DEBUG) {
                Log.d(
                    TAG,
                    "Google sign-in success (authorizedOnly=$filterByAuthorizedAccounts), providerId=${c.id}"
                )
                Log.d(
                        TAG,
                "Google sign-in success (authorizedOnly=$filterByAuthorizedAccounts), idToken=${c.idToken}"
                )
            }

            return SocialSignInResult.Success(
                SocialUser(
                    provider = SocialProvider.GOOGLE,
                    idToken = c.idToken,
                    accessToken = null,
                    email = c.id,
                    name = c.displayName
                )
            )
        } else {
            val error = IllegalArgumentException("Unsupported credential type: ${credential::class.java}")
            Log.e(TAG, "Unexpected credential type", error)
            return SocialSignInResult.Error(error)
        }
    }


    override suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}
