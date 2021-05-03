#include "../ABY/src/abycore/circuit/booleancircuits.h"
#include "../ABY/src/abycore/circuit/arithmeticcircuits.h"
#include "../ABY/src/abycore/circuit/circuit.h"
#include "../ABY/src/abycore/sharing/sharing.h"
#include "../ABY/src/abycore/aby/abyparty.h"
#include "../ABY/src/abycore/ABY_utils/ABYconstants.h"
#include <math.h>
#include <map>
#include <cassert>

using namespace std;

enum SingleCircuitType
{
    CIRC_LESS_THAN,
    CIRC_GREATER_THAN,
    CIRC_EQUAL,
    CIRC_AND,
    CIRC_OR
};

enum MultiCircuitType
{
    CIRC_RANGE,
    CIRC_WITHIN
};

ABYParty *createServer(string ip_address = "127.0.0.1", int port = 7766U, seclvl seclvl = LT, uint32_t bitlen = 32U, uint32_t threads = 1U, string aby_circ_dir = "./extern/ABY/bin/circ");
ABYParty *createClient(string ip_address = "127.0.0.1", int port = 7766U, seclvl seclvl = LT, uint32_t bitlen = 32U, uint32_t threads = 1U, string aby_circ_dir = "./extern/ABY/bin/circ");
Circuit *createCircuit(ABYParty *party);
share *megaCircSingleInputServer(ABYParty *party, uint32_t input, SingleCircuitType circ, uint32_t bitlen = 32);
share *megaCircSingleInputClient(ABYParty *party, uint32_t input, uint32_t bitlen = 32);
share *range_inner(BooleanCircuit *bc, share *x_server, share *y_server, share *x_client, share *y_client);
share *within_inner(BooleanCircuit *bc, share *x_1_server, share *x_2_server, share *y_1_server, share *y_2_server, share *x_client, share *y_client);
share *megaCircMultiInputServer(ABYParty *party, vector<uint32_t> input, MultiCircuitType circ, uint32_t bitlen = 32);
share *megaCircMultiInputClient(ABYParty *party, vector<uint32_t> input, uint32_t bitlen = 32);