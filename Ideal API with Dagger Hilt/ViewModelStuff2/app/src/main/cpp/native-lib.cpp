// importing libraries
#include <jni.h>
#include <string>

// Store AUTH Tokens
// Store Base URLs
// Create common objects that can be shared across platforms
// String Arrays
// Do heavy computation (bitmap, string, encryption, etc) in this class and get the result - for speed

// Method convention: Java_PackageName_ActivityName_MethodName
extern "C"
JNIEXPORT jstring JNICALL
Java_com_singularitycoder_viewmodelstuff2_helpers_JniDefinitions_getApIAuthToken(
        JNIEnv *env,
        jobject thiz,
        jint apiType) {

    std::string authToken = "Bearer";
    switch (apiType) {
        case 0:
            authToken = "ANI_API_AUTH_TOKEN";
            break;
        case 1:
            authToken = "GITHUB_GRAPH_QL_API_AUTH_TOKEN";
            break;
        default:
            break;
    }
    return env->NewStringUTF(authToken.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_singularitycoder_viewmodelstuff2_helpers_JniDefinitions_getAniApiBaseUrl(JNIEnv *env, jobject thiz) {
    std::string authToken = "https://api.aniapi.com";
    return env->NewStringUTF(authToken.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_singularitycoder_viewmodelstuff2_helpers_JniDefinitions_getGithubApiBaseUrl(JNIEnv *env, jobject thiz) {
    std::string authToken = "https://api.github.com/";
    return env->NewStringUTF(authToken.c_str());
}