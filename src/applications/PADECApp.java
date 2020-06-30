package applications;

import core.*;
import padec.application.Endpoint;
import padec.application.LocationEndpoint;
import padec.attribute.Location;
import padec.attribute.PADECContext;
import padec.attribute.Pair;
import padec.filtering.FilteredData;
import padec.filtering.techniques.BasicFuzzy;
import padec.filtering.techniques.PairFuzzy;
import padec.key.Key;
import padec.lock.AccessLevel;
import padec.lock.Keyhole;
import padec.lock.Lock;
import padec.rule.BaseRule;
import padec.rule.ComposedRule;
import padec.rule.Rule;
import padec.rule.operator.AndOperator;
import padec.rule.operator.GreaterThanOperator;
import padec.rule.operator.LessThanOperator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class PADECApp extends Application {

    private abstract static class RuleProvider{
        private static final int IN_0_100_RANGE_RULE = 0;

        private static Rule in0_100Range(){
            Rule withinAreaMax = new BaseRule(Location.class, new Pair[]{new Pair<>(200000000000., 200000000000.)}, new LessThanOperator());
            Rule withinAreaMin = new BaseRule(Location.class, new Pair[]{new Pair<>(-200000000000., -200000000000.)}, new GreaterThanOperator());
            return new ComposedRule(withinAreaMax, withinAreaMin, new AndOperator());
        }

        public static Rule getRule(Integer defRule){
            switch (defRule){
                case IN_0_100_RANGE_RULE:
                    return in0_100Range();
                default:
                    return null;
            }
        }
    }

    /** Seed for the app's random number generator **/
    public static final String PADEC_SEED = "seed";
    /** Destination address range - inclusive lower, exclusive upper **/
    public static final String PADEC_DEST_RANGE = "destinationRange";
    /** Act as provider **/
    public static final String PADEC_PROVIDER = "provider";
    /** Act as consumer **/
    public static final String PADEC_CONSUMER = "consumer";
    /** Act as attacker **/
    public static final String PADEC_ATTACKER = "attacker";
    /** Defines the rule to be used as default **/
    public static final String PADEC_DEFAULT_RULE = "defrule";
    /** (Minimum) interval between consumer requests **/
    public static final String PADEC_REQUEST_INTERVAL = "interval";

    /** Key that stores the message type **/
    public static final String MSG_TYPE = "type";

    /** Key that stores the access level in a keyhole request **/
    public static final String KH_REQ_ACCESS_LEVEL = "khr-access-level";

    /** Key that stores the keyhole in a keyhole answer **/
    public static final String KH_ANSW_KEYHOLE = "kha-keyhole";

    /** Key that stores the key in a key message **/
    public static final String KEY_KEY = "k-key";
    /** Key that stores the access level in a key message **/
    public static final String KEY_ACCESS_LEVEL = "k-access-level";
    /** Key that stores the endpoint parameters in a key message **/
    public static final String KEY_ENDPOINT_PARAMS = "k-endpoint-params";

    /** Key that stores the data in an information answer **/
    public static final String INFO_DATA = "i-data";

    /** Message type: keyhole request **/
    public static final int MSG_TYPE_KEYHOLE_REQUEST = 0;
    /** Message type: keyhole **/
    public static final int MSG_TYPE_KEYHOLE = 1;
    /** Message type: key **/
    public static final int MSG_TYPE_KEY = 2;
    /** Message type: information from endpoint **/
    public static final int MSG_TYPE_INFO = 3;
    /** Message type: access denied **/
    public static final int MSG_TYPE_DENIED = 4;

    /** Special value for max access level **/
    public static final int ACCESS_LEVEL_MAX = -1;

    /** Application ID */
    public static final String APP_ID = "ut.unex.PADECApplication";

    // Private vars
    private int seed = 0;
    private int destMin = 0;
    private int destMax = 1;
    private int defaultRule = 0;
    private boolean provider = false;
    private boolean consumer = false;
    private boolean attacker = false;
    private double interval = 500;
    private Random rng;

    //Static vars for managing PADEC-specific entities
    /** Maps contexts by address. Lets the app get the context of a host, avoiding context collision between hosts **/
    private static Map<Integer, PADECContext> contexts = null;
    /** Maps locks by address **/
    private static Map<Integer, Lock> locks = null;
    /** Maps endpoints by address **/
    private static Map<Integer, Endpoint> endpoints = null;
    /** Maps the moment of the last request by address **/
    private static Map<Integer, Double> lastRequest = null;

    /**
     * Creates a new PADEC application with the given settings.
     *
     * @param s	Settings to use for initializing the application.
     */
    public PADECApp(Settings s) {
        if (s.contains(PADEC_PROVIDER)){
            this.provider = s.getBoolean(PADEC_PROVIDER);
        }
        if (s.contains(PADEC_CONSUMER)){
            this.consumer = s.getBoolean(PADEC_CONSUMER);
        }
        if (s.contains(PADEC_ATTACKER)){
            this.attacker = s.getBoolean(PADEC_ATTACKER);
        }
        if (s.contains(PADEC_DEFAULT_RULE)){
            this.defaultRule = s.getInt(PADEC_DEFAULT_RULE);
        }
        if (s.contains(PADEC_REQUEST_INTERVAL)){
            this.interval = s.getDouble(PADEC_REQUEST_INTERVAL);
        }
        if (s.contains(PADEC_SEED)){
            this.seed = s.getInt(PADEC_SEED);
        }
        if (s.contains(PADEC_DEST_RANGE)){
            int[] destination = s.getCsvInts(PADEC_DEST_RANGE,2);
            this.destMin = destination[0];
            this.destMax = destination[1];
        }

        rng = new Random(this.seed);
        if (contexts == null){
            contexts = new LinkedHashMap<>();
        }
        if (locks == null){
            locks = new LinkedHashMap<>();
        }
        if (endpoints == null){
            endpoints = new LinkedHashMap<>();
        }
        if (lastRequest == null){
            lastRequest = new LinkedHashMap<>();
        }
        super.setAppID(APP_ID);
    }

    public int getDestMin() {
        return destMin;
    }

    public void setDestMin(int destMin) {
        this.destMin = destMin;
    }

    public int getDestMax() {
        return destMax;
    }

    public void setDestMax(int destMax) {
        this.destMax = destMax;
    }

    public boolean isProvider() {
        return provider;
    }

    public boolean isConsumer() {
        return consumer;
    }

    public boolean isAttacker() {
        return attacker;
    }

    public int getDefaultRule() {
        return defaultRule;
    }

    public double getInterval() {
        return interval;
    }

    /**
     * Copy-constructor
     *
     * @param a PADEC Application
     */
    public PADECApp(PADECApp a) {
        super(a);
        this.seed = a.getSeed();
        this.provider = a.isProvider();
        this.consumer = a.isConsumer();
        this.attacker = a.isAttacker();
        this.destMin = a.getDestMin();
        this.destMax = a.getDestMax();
        this.defaultRule = a.getDefaultRule();
        this.interval = a.getInterval();
        this.rng = new Random(this.seed);
    }

    private Message consumerHandle(Message msg, DTNHost host){
        if(!contexts.containsKey(host.getAddress())){ //If there is no context yet
            PADECContext cntxt = new PADECContext(); // Create it
            cntxt.registerAttribute(Location.class); // Register location
            contexts.put(host.getAddress(), cntxt); // Save it
        }
        Integer type = (Integer) msg.getProperty(MSG_TYPE);
        switch (type){
            case MSG_TYPE_KEYHOLE:
                Keyhole kh = (Keyhole) msg.getProperty(KH_ANSW_KEYHOLE);
                PADECContext cntxt = contexts.get(host.getAddress());
                cntxt.getAttribute(Location.class).setValue(
                        new Pair<>(host.getLocation().getX(), host.getLocation().getY())); // Update location
                Key key = new Key(kh, cntxt);
                String id = "k-"+host.getAddress()+"-"+msg.getFrom().getAddress()+"@"+SimClock.getIntTime();
                Message m = new Message(host, msg.getFrom(), id, 1);
                m.addProperty(MSG_TYPE, MSG_TYPE_KEY);
                m.addProperty(KEY_KEY, key);
                m.addProperty(KEY_ENDPOINT_PARAMS, new Object[]{});
                m.addProperty(KEY_ACCESS_LEVEL, ACCESS_LEVEL_MAX);
                m.setAppID(APP_ID);
                super.sendEventToListeners("GotKeyhole", null, host);
                host.createNewMessage(m);
                break;
            case MSG_TYPE_INFO:
                FilteredData data = (FilteredData) msg.getProperty(INFO_DATA);
                System.out.println("Host "+host.getAddress()+": Access granted. Info: " + data.getData() + ". Precision: " + data.getPrecision());
                lastRequest.put(host.getAddress(), -1*lastRequest.get(host.getAddress()));
                super.sendEventToListeners("AccessGranted", null, host);
                break;
            case MSG_TYPE_DENIED:
                System.out.println("Host "+host.getAddress()+": Access denied.");
                lastRequest.put(host.getAddress(), -1*lastRequest.get(host.getAddress()));
                super.sendEventToListeners("AccessDenied", null, host);
                break;
        }
        return msg;
    }

    private Message providerHandle(Message msg, DTNHost host){
        if(!locks.containsKey(host.getAddress())){ //If there is no lock yet
            if(!endpoints.containsKey(host.getAddress())){ // If there is no endpoint yet
                endpoints.put(host.getAddress(), new LocationEndpoint()); // Create it
            }
            Rule mRule = RuleProvider.getRule(defaultRule); // Get the default rule
            AccessLevel al = new AccessLevel(new PairFuzzy(),
                    endpoints.get(host.getAddress()), new Double[]{1.0}, mRule); // Create an access level with that rule
            Lock lock = new Lock(); // Create a lock
            lock.addAccessLevel(al); // Add the access level to the lock
            locks.put(host.getAddress(), lock); // Save the access level
        }
        Integer type = (Integer) msg.getProperty(MSG_TYPE);
        switch (type){
            case MSG_TYPE_KEYHOLE_REQUEST:
                Integer al = (Integer) msg.getProperty(KH_REQ_ACCESS_LEVEL);
                Keyhole retKh;
                if (al == ACCESS_LEVEL_MAX){
                    AccessLevel mAl = locks.get(host.getAddress()).getMaxAccessLevel();
                    retKh = mAl.getKeyhole();
                }
                else{
                    AccessLevel mAl = locks.get(host.getAddress()).getAccessLevel(al);
                    mAl = mAl == null ? locks.get(host.getAddress()).getMaxAccessLevel() : mAl; // If they asked for a
                    // higher access level than the one in existence, get them the maximum one.
                    retKh = mAl.getKeyhole();
                }
                String id = "kha-"+host.getAddress()+"-"+msg.getFrom().getAddress()+"@"+SimClock.getIntTime();
                Message m = new Message(host, msg.getFrom(), id, 1);
                m.addProperty(MSG_TYPE, MSG_TYPE_KEYHOLE);
                m.addProperty(KH_ANSW_KEYHOLE, retKh);
                m.setAppID(APP_ID);
                host.createNewMessage(m);
                super.sendEventToListeners("GotKeyholeRequest", null, host);
                break;
            case MSG_TYPE_KEY:
                Key k = (Key) msg.getProperty(KEY_KEY);
                al = (Integer) msg.getProperty(KEY_ACCESS_LEVEL);
                AccessLevel rAl;
                if (al == ACCESS_LEVEL_MAX){
                    rAl = locks.get(host.getAddress()).getMaxAccessLevel();
                }
                else{
                    rAl = locks.get(host.getAddress()).getAccessLevel(al);
                    rAl = rAl == null ? locks.get(host.getAddress()).getMaxAccessLevel() : rAl;
                }
                Object[] params = (Object[]) msg.getProperty(KEY_ENDPOINT_PARAMS);

                // Update location endpoint - just in case
                ((LocationEndpoint) endpoints.get(host.getAddress())).updateLocation(
                        new Pair<>(host.getLocation().getX(), host.getLocation().getY()));

                FilteredData result = rAl.testAccess(params, k);
                id = "resp-"+host.getAddress()+"-"+msg.getFrom().getAddress()+"@"+SimClock.getIntTime();
                m = new Message(host, msg.getFrom(), id, 1);
                if (result != null){ // Key accepted
                    m.addProperty(MSG_TYPE, MSG_TYPE_INFO);
                    m.addProperty(INFO_DATA, result);
                }
                else{ // Key rejected
                    m.addProperty(MSG_TYPE, MSG_TYPE_DENIED);
                }
                m.setAppID(APP_ID);
                host.createNewMessage(m);
                super.sendEventToListeners("GotKey", null, host);
                break;
        }
        return msg;
    }

    private Message attackerHandle(Message msg, DTNHost host){
        //TODO Choose attacker behavior
        return msg;
    }

    private Message providerAndConsumerHandle(Message msg, DTNHost host){
        providerHandle(msg, host);
        consumerHandle(msg, host);
        return msg;
    }

    /**
     * Handles an incoming message. Generates events for messages.
     *
     * @param msg	message received by the router
     * @param host	host to which the application instance is attached
     */
    @Override
    public Message handle(Message msg, DTNHost host){
        Integer type = (Integer) msg.getProperty(MSG_TYPE);
        if (type == null || msg.getTo() != host){
            return msg; // Not a PADEC message, or not directed to that host
        }
        if (isConsumer() && isProvider()){
            return providerAndConsumerHandle(msg, host);
        }
        if (isConsumer()){
            return consumerHandle(msg, host);
        }
        if (isProvider()){
            return providerHandle(msg, host);
        }
        if (isAttacker()){
            return attackerHandle(msg, host);
        }
        return msg;
    }

    /**
     * Draws a random host from the destination range
     *
     * @return host
     */
    private DTNHost randomHost() {
        int destaddr = 0;
        World w = SimScenario.getInstance().getWorld();
        if (destMax == destMin) {
            destaddr = destMin;
        }
        else {
            destaddr = destMin + rng.nextInt(destMax - destMin);
        }
        return w.getNodeByAddress(destaddr);
    }

    @Override
    public Application replicate(){
        return new PADECApp(this);
    }

    /**
     * Sends a request if this is a PADEC consumer instance.
     *
     * @param host to which the application instance is attached
     */
    @Override
    public void update(DTNHost host) {
        if (isConsumer()){
            double curTime = SimClock.getTime();
            if (lastRequest.containsKey(host.getAddress())){ // If I have sent a request previously
                double lastReq = lastRequest.get(host.getAddress());
                if (lastReq < 0 || curTime - lastReq < this.interval) { // If I'm waiting for an answer or it is too early
                    return; // Do nothing
                }
            }
            // In any other case, send a keyhole request
            DTNHost destination = randomHost();
            String id = "khr-"+host.getAddress()+"-"+destination.getAddress()+"@"+SimClock.getIntTime();
            Message m = new Message(host, destination, id, 1);
            m.addProperty(MSG_TYPE, MSG_TYPE_KEYHOLE_REQUEST);
            m.addProperty(KH_REQ_ACCESS_LEVEL, ACCESS_LEVEL_MAX);
            m.setAppID(APP_ID);
            super.sendEventToListeners("PADECRequest", null, host);
            host.createNewMessage(m);
            lastRequest.put(host.getAddress(), -1*curTime); // The message is sent, waiting for an answer
        }
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }
}
