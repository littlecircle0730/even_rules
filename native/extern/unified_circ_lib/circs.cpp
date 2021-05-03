#include "circs.h"

using namespace std;

ABYParty *createServer(string ip_address, int port, seclvl seclvl, uint32_t bitlen, uint32_t threads, string aby_circ_dir)
{
    return new ABYParty(SERVER, ip_address, (uint16_t)port, seclvl, bitlen, threads, MT_OT, 65536U, aby_circ_dir);
}

ABYParty *createClient(string ip_address, int port, seclvl seclvl, uint32_t bitlen, uint32_t threads, string aby_circ_dir)
{
    return new ABYParty(CLIENT, ip_address, (uint16_t)port, seclvl, bitlen, threads, MT_OT, 65536U, aby_circ_dir);
}

Circuit *createCircuit(ABYParty *party, e_sharing chosen_sharing)
{
    vector<Sharing *> &sharings = party->GetSharings();
    Circuit *circuit = sharings[chosen_sharing]->GetCircuitBuildRoutine();
    return circuit;
}

share *megaCircSingleInputServer(ABYParty *party, uint32_t input, SingleCircuitType circ, uint32_t bitlen)
{
    BooleanCircuit *circuit = (BooleanCircuit *)createCircuit(party, S_BOOL);

    share *client_input = circuit->PutDummyINGate(bitlen);
    share *server_input = circuit->PutINGate(input, bitlen, SERVER);

    // MUX design is the following:
    // Tier 1 -> Decides if the operation is "Equal" (1) or not (0)
    // Tier 2 -> If tier 1 is 0, decides between "Greater/Less" (0) or "And/Or" (1).
    // Tier 3 -> Decides between "Greater" (0), "Less" (1), "And" (0) and "Or" (1), according to previous tiers.
    uint32_t tier_1_value, tier_2_value, tier_3_value;

    switch (circ)
    {
    case CIRC_GREATER_THAN:
        tier_1_value = 0;
        tier_2_value = 0;
        tier_3_value = 0;
        break;
    case CIRC_LESS_THAN:
        tier_1_value = 0;
        tier_2_value = 0;
        tier_3_value = 1;
        break;
    case CIRC_AND:
        tier_1_value = 0;
        tier_2_value = 1;
        tier_3_value = 0;
        break;
    case CIRC_OR:
        tier_1_value = 0;
        tier_2_value = 1;
        tier_3_value = 1;
        break;
    case CIRC_EQUAL:
        tier_1_value = 1;
        tier_2_value = 0;
        tier_3_value = 0;
        break;
    }

    share *tier_1 = circuit->PutINGate(tier_1_value, 1, SERVER);
    share *tier_2 = circuit->PutINGate(tier_2_value, 1, SERVER);
    share *tier_3 = circuit->PutINGate(tier_3_value, 1, SERVER);

    share *s_greater = circuit->PutGTGate(client_input, server_input);
    share *s_less = circuit->PutGTGate(server_input, client_input);
    share *s_equal = circuit->PutANDGate(circuit->PutINVGate(s_greater), circuit->PutINVGate(s_less));
    share *s_and = circuit->PutANDGate(client_input, server_input);
    share *s_or = circuit->PutORGate(client_input, server_input);

    // Keep in mind: This outputs ina in case sel=1 and inb in case sel=0
    share *tier_3_mux_a = circuit->PutMUXGate(s_less, s_greater, tier_3);
    share *tier_3_mux_b = circuit->PutMUXGate(s_or, s_and, tier_3);

    share *tier_2_mux = circuit->PutMUXGate(tier_3_mux_b, tier_3_mux_a, tier_2);

    share *tier_1_mux = circuit->PutMUXGate(s_equal, tier_2_mux, tier_1);

    share *s_out = circuit->PutOUTGate(tier_1_mux, SERVER);

    return s_out;
}

share *megaCircSingleInputClient(ABYParty *party, uint32_t input, uint32_t bitlen)
{
    BooleanCircuit *circuit = (BooleanCircuit *)createCircuit(party, S_BOOL);

    share *client_input = circuit->PutINGate(input, bitlen, CLIENT);
    share *server_input = circuit->PutDummyINGate(bitlen);

    share *tier_1 = circuit->PutDummyINGate(1);
    share *tier_2 = circuit->PutDummyINGate(1);
    share *tier_3 = circuit->PutDummyINGate(1);

    share *s_greater = circuit->PutGTGate(client_input, server_input);
    share *s_less = circuit->PutGTGate(server_input, client_input);
    share *s_equal = circuit->PutANDGate(circuit->PutINVGate(s_greater), circuit->PutINVGate(s_less));
    share *s_and = circuit->PutANDGate(client_input, server_input);
    share *s_or = circuit->PutORGate(client_input, server_input);

    // Keep in mind: This outputs ina in case sel=1 and inb in case sel=0
    share *tier_3_mux_a = circuit->PutMUXGate(s_less, s_greater, tier_3);
    share *tier_3_mux_b = circuit->PutMUXGate(s_or, s_and, tier_3);

    share *tier_2_mux = circuit->PutMUXGate(tier_3_mux_b, tier_3_mux_a, tier_2);

    share *tier_1_mux = circuit->PutMUXGate(s_equal, tier_2_mux, tier_1);

    share *s_out = circuit->PutOUTGate(tier_1_mux, SERVER);

    return s_out;
}

share *range_inner(BooleanCircuit *bc, share *x_server, share *y_server, share *x_client, share *y_client)
{
    share *check_x, *check_x_inv, *t_a, *t_b, *res_x, *s_out;

    // (x1-x2)^2
    check_x = bc->PutGTGate(x_server, x_client); // Discover the larger X value
    check_x_inv = bc->PutINVGate(check_x);
    t_a = bc->PutMUXGate(x_server, x_client, check_x);     // Larger X
    t_b = bc->PutMUXGate(x_server, x_client, check_x_inv); // Smaller X
    res_x = bc->PutSUBGate(t_a, t_b);                      //(Larger X - Smaller X)
    res_x = bc->PutMULGate(res_x, res_x);                  // (Larger X - Smaller X)^2

    share *check_y, *check_y_inv, *res_y;
    // (y1-y2)^2
    check_y = bc->PutGTGate(y_server, y_client); // Discover the larger y value
    check_y_inv = bc->PutINVGate(check_y);
    t_a = bc->PutMUXGate(y_server, y_client, check_y);     // Larger y
    t_b = bc->PutMUXGate(y_server, y_client, check_y_inv); // Smaller y
    res_y = bc->PutSUBGate(t_a, t_b);                      //(Larger y - Smaller y)
    res_y = bc->PutMULGate(res_y, res_y);                  // (Larger y - Smaller y)^2

    s_out = bc->PutADDGate(res_x, res_y);

    return s_out;
}

share *within_inner(BooleanCircuit *bc, share *x_1_server, share *x_2_server, share *y_1_server, share *y_2_server, share *x_client, share *y_client)
{
    share *check_x, *larger_x, *smaller_x, *check_y, *larger_y, *smaller_y;

    check_x = bc->PutGTGate(x_1_server, x_2_server);
    larger_x = bc->PutMUXGate(x_1_server, x_2_server, check_x);
    smaller_x = bc->PutMUXGate(x_2_server, x_1_server, check_x);

    check_y = bc->PutGTGate(y_1_server, y_2_server);
    larger_y = bc->PutMUXGate(y_1_server, y_2_server, check_y);
    smaller_y = bc->PutMUXGate(y_2_server, y_1_server, check_y);

    share *less_than_x, *greater_than_x, *less_than_y, *greater_than_y;

    less_than_x = bc->PutGTGate(larger_x, x_client);
    greater_than_x = bc->PutGTGate(x_client, smaller_x);

    less_than_y = bc->PutGTGate(larger_y, y_client);
    greater_than_y = bc->PutGTGate(y_client, smaller_y);

    share *res_x, *res_y;

    res_x = bc->PutANDGate(less_than_x, greater_than_x);
    res_y = bc->PutANDGate(less_than_y, greater_than_y);

    share *s_out = bc->PutANDGate(res_x, res_y);

    return s_out;
}

share *megaCircMultiInputServer(ABYParty *party, vector<uint32_t> input, MultiCircuitType circ, uint32_t bitlen)
{
    BooleanCircuit *circuit = (BooleanCircuit *)createCircuit(party, S_BOOL);

    share *x_1_server, *x_2_server, *x_client, *y_1_server, *y_2_server, *y_client;

    uint32_t tier_1;
    switch (circ)
    {
    case CIRC_RANGE:
        if (input.size() != 2)
        {
            cerr << "Warning!! Range requested, but input size was " << input.size() << endl;
            if (input.size() > 2)
            {
                cerr << "Only the first two inputs will be considered!!!" << endl;
            }
            else
            {
                cerr << "Not enough inputs. Aborting..." << endl;
                return nullptr;
            }
        }
        x_1_server = circuit->PutINGate(input[0], bitlen, SERVER);
        x_2_server = circuit->PutINGate(input[0], bitlen, SERVER);
        y_1_server = circuit->PutINGate(input[1], bitlen, SERVER);
        y_2_server = circuit->PutINGate(input[1], bitlen, SERVER);
        tier_1 = 0;
        break;
    case CIRC_WITHIN:
        if (input.size() != 4)
        {
            cerr << "Warning!! Within requested, but input size was " << input.size() << endl;
            if (input.size() > 4)
            {
                cerr << "Only the first four inputs will be considered!!!" << endl;
            }
            else
            {
                cerr << "Not enough inputs. Aborting..." << endl;
                return nullptr;
            }
        }
        x_1_server = circuit->PutINGate(input[0], bitlen, SERVER);
        x_2_server = circuit->PutINGate(input[2], bitlen, SERVER);
        y_1_server = circuit->PutINGate(input[1], bitlen, SERVER);
        y_2_server = circuit->PutINGate(input[3], bitlen, SERVER);
        tier_1 = 1;
        break;
    }

    x_client = circuit->PutDummyINGate(bitlen);
    y_client = circuit->PutDummyINGate(bitlen);

    share *tier_1_sel = circuit->PutINGate(tier_1, 1, SERVER);

    share *s_range = range_inner(circuit, x_1_server, y_1_server, x_client, y_client);
    share *s_within = within_inner(circuit, x_1_server, x_2_server, y_1_server, y_2_server, x_client, y_client);

    share *tier_1_mux = circuit->PutMUXGate(s_within, s_range, tier_1_sel);

    share *s_out = circuit->PutOUTGate(tier_1_mux, SERVER);

    return s_out;
}

share *megaCircMultiInputClient(ABYParty *party, vector<uint32_t> input, uint32_t bitlen)
{
    BooleanCircuit *circuit = (BooleanCircuit *)createCircuit(party, S_BOOL);

    share *x_1_server, *x_2_server, *x_client, *y_1_server, *y_2_server, *y_client;

    x_1_server = circuit->PutDummyINGate(bitlen);
    x_2_server = circuit->PutDummyINGate(bitlen);
    y_1_server = circuit->PutDummyINGate(bitlen);
    y_2_server = circuit->PutDummyINGate(bitlen);

    x_client = circuit->PutINGate(input[0], bitlen, CLIENT);
    y_client = circuit->PutINGate(input[1], bitlen, CLIENT);

    share *tier_1_sel = circuit->PutDummyINGate(1);

    share *s_range = range_inner(circuit, x_1_server, y_1_server, x_client, y_client);
    share *s_within = within_inner(circuit, x_1_server, x_2_server, y_1_server, y_2_server, x_client, y_client);

    share *tier_1_mux = circuit->PutMUXGate(s_within, s_range, tier_1_sel);

    share *s_out = circuit->PutOUTGate(tier_1_mux, SERVER);

    return s_out;
}