#include "padec_natpsi_NativeMeth.h"
#include "extern/unified_circ_lib/circs.h"

/*
 * Class:     padec_natpsi_NativeMeth
 * Method:    singleInputServer
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_padec_natpsi_NativeMeth_singleInputServer(JNIEnv *env, jobject obj, jint input, jint type)
{
    SingleCircuitType s_type = static_cast<SingleCircuitType>(type);

    ABYParty *party = createServer();

    share* output = megaCircSingleInputServer(party, (uint32_t)input, s_type);

    party->ExecCircuit();

    uint32_t clear_out = output->get_clear_value<uint32_t>();

    delete party;

    return (jint)clear_out;
}

/*
 * Class:     padec_natpsi_NativeMeth
 * Method:    singleInputClient
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_padec_natpsi_NativeMeth_singleInputClient(JNIEnv *env, jobject obj, jint input){

    ABYParty *party = createClient();

    megaCircSingleInputClient(party, (uint32_t)input);

    party->ExecCircuit();

    delete party;
}

/*
 * Class:     padec_natpsi_NativeMeth
 * Method:    multiInputServer
 * Signature: ([II)I
 */
JNIEXPORT jint JNICALL Java_padec_natpsi_NativeMeth_multiInputServer(JNIEnv *env, jobject obj, jintArray input, jint type){
    MultiCircuitType m_type = static_cast<MultiCircuitType>(type);

    jsize len = env->GetArrayLength(input);
    jint *input_array = env->GetIntArrayElements(input, 0);
    vector<uint32_t> input_vec;
    for(int i=0;i<len;i++){
        input_vec.push_back((uint32_t) input_array[i]);
    }
    env->ReleaseIntArrayElements(input, input_array, 0);

    ABYParty *party = createServer();

    share *output = megaCircMultiInputServer(party, input_vec, m_type);

    party->ExecCircuit();

    uint32_t clear_out = output->get_clear_value<uint32_t>();

    delete party;

    return (jint)clear_out;
}

/*
 * Class:     padec_natpsi_NativeMeth
 * Method:    multiInputClient
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL Java_padec_natpsi_NativeMeth_multiInputClient(JNIEnv *env, jobject obj, jintArray input){
    jsize len = env->GetArrayLength(input);
    jint *input_array = env->GetIntArrayElements(input, 0);
    vector<uint32_t> input_vec;
    for (int i = 0; i < len; i++)
    {
        input_vec.push_back((uint32_t)input_array[i]);
    }
    env->ReleaseIntArrayElements(input, input_array, 0);

    ABYParty *party = createClient();

    megaCircMultiInputClient(party, input_vec);

    party->ExecCircuit();

    delete party;
}