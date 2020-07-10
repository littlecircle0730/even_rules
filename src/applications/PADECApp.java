package applications;

import core.*;
import padec.application.Endpoint;
import padec.application.HistoryEndpoint;
import padec.application.LocationEndpoint;
import padec.attribute.Location;
import padec.attribute.PADECContext;
import padec.attribute.Pair;
import padec.attribute.SoundLevel;
import padec.crypto.SimpleCrypto;
import padec.filtering.FilteredData;
import padec.filtering.techniques.HistoryFuzzy;
import padec.filtering.techniques.PairFuzzy;
import padec.key.Key;
import padec.lock.AccessLevel;
import padec.lock.Keyhole;
import padec.lock.Lock;
import padec.rule.ComposedRule;
import padec.rule.DualRule;
import padec.rule.Rule;
import padec.rule.operator.AndOperator;
import padec.rule.operator.EqualOperator;
import padec.rule.operator.LessThanOperator;
import padec.rule.operator.RangeOperator;

import java.security.KeyPair;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PADECApp extends Application {

    private abstract static class RuleProvider{

        private static Rule in0_100Range(PADECContext context){
            return new DualRule(Location.class, new Double[]{10000.0}, new RangeOperator(), new LessThanOperator(), context);
        }

        private static Rule closeBy(PADECContext context){
            return new DualRule(Location.class, new Double[]{500.0}, new RangeOperator(), new LessThanOperator(), context);
        }

        private static Rule in0_100RangePlusSameSound(PADECContext context){
            Rule base = new DualRule(Location.class, new Double[]{500.0}, new RangeOperator(), new LessThanOperator(), context);
            Rule sameSound = new DualRule(SoundLevel.class, new Boolean[]{Boolean.TRUE}, new EqualOperator(), new EqualOperator(), context);
            return new ComposedRule(base, sameSound, new AndOperator());
        }
    }

    private abstract static class LockProvider{
        private static final int LOCK_BASE = 0;
        private static final int LOCK_2_AL = 1;
        private static final int LOCK_3_AL = 2;
        private static final int LOCK_HISTORY = 3;

        private static Lock baseLock(Endpoint endpoint, PADECContext context){
            Rule mRule = RuleProvider.in0_100Range(context);
            Lock lock = new Lock(endpoint);
            lock.addAccessLevel(new PairFuzzy(), Collections.singletonMap(PairFuzzy.PRECISION_KEY, 1.0), mRule);
            return lock;
        }

        private static Lock lock2Al(Endpoint endpoint, PADECContext context){
            Rule botRule = RuleProvider.in0_100Range(context);
            Rule topRule = RuleProvider.in0_100RangePlusSameSound(context);
            Lock lock = new Lock(endpoint);
            lock.addAccessLevel(new PairFuzzy(), Collections.singletonMap(PairFuzzy.PRECISION_KEY, 50.0), botRule);
            lock.addAccessLevel(new PairFuzzy(), Collections.singletonMap(PairFuzzy.PRECISION_KEY, 1.0), topRule);
            return lock;
        }

        private static Lock lock3Al(Endpoint endpoint, PADECContext context){
            Rule botRule = RuleProvider.in0_100Range(context);
            Rule midRule = RuleProvider.closeBy(context);
            Rule topRule = RuleProvider.in0_100RangePlusSameSound(context);
            Lock lock = new Lock(endpoint);
            lock.addAccessLevel(new PairFuzzy(), Collections.singletonMap(PairFuzzy.PRECISION_KEY, 50.0), botRule);
            lock.addAccessLevel(new PairFuzzy(), Collections.singletonMap(PairFuzzy.PRECISION_KEY, 20.0), midRule);
            lock.addAccessLevel(new PairFuzzy(), Collections.singletonMap(PairFuzzy.PRECISION_KEY, 0.0), topRule);
            return lock;
        }

        private static Lock lockHistory(Endpoint endpoint, PADECContext context) {
            Rule botRule = RuleProvider.in0_100Range(context);
            Rule midRule = RuleProvider.closeBy(context);
            Rule topRule = RuleProvider.in0_100RangePlusSameSound(context);
            Lock lock = new Lock(endpoint);

            Map<String, Object> botParams = new HashMap<>(), midParams = new HashMap<>();
            botParams.put(HistoryFuzzy.AT_LEAST_TIMES_KEY, 3);
            try {
                botParams.put(HistoryFuzzy.BEFORE_DATE_KEY, new SimpleDateFormat("yyyy-MM-dd").parse("2020-03-01"));
                botParams.put(HistoryFuzzy.AFTER_DATE_KEY, new SimpleDateFormat("yyyy-MM-dd").parse("2020-01-01"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            lock.addAccessLevel(new HistoryFuzzy(), botParams, botRule);

            midParams.put(HistoryFuzzy.AT_LEAST_TIMES_KEY, 3);
            try {
                midParams.put(HistoryFuzzy.AFTER_DATE_KEY, new SimpleDateFormat("yyyy-MM-dd").parse("2020-03-01"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            lock.addAccessLevel(new HistoryFuzzy(), midParams, midRule);

            lock.addAccessLevel(new HistoryFuzzy(), null, topRule);
            return lock;
        }

        public static Lock getLock(Endpoint endpoint, PADECContext context, int lock){
            switch (lock) {
                case LOCK_BASE:
                    return baseLock(endpoint, context);
                case LOCK_2_AL:
                    return lock2Al(endpoint, context);
                case LOCK_3_AL:
                    return lock3Al(endpoint, context);
                case LOCK_HISTORY:
                    return lockHistory(endpoint, context);
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
    public static final String PADEC_DEFAULT_LOCK = "deflock";
    /** (Minimum) interval between consumer requests **/
    public static final String PADEC_REQUEST_INTERVAL = "interval";
    /** Requested precision **/
    public static final String PADEC_REQUESTED_PRECISION = "reqprec";

    /** Key that stores the message type **/
    public static final String MSG_TYPE = "type";

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
    private int defaultLock = 0;
    private double requestedPrecision = 0.0;
    private boolean provider = false;
    private boolean consumer = false;
    private boolean attacker = false;
    private double interval = 500;
    private Random rng;

    //Static vars for managing PADEC-specific entities
    /** Maps contexts by address. Lets the app get the context of a host, avoiding context collision between hosts **/
    private static Map<Integer, PADECContext> contexts = null;
    /** Maps crypto-keypairs by address **/
    private static Map<Integer, KeyPair> cryptoKeys = null;
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
        if (s.contains(PADEC_DEFAULT_LOCK)){
            this.defaultLock = s.getInt(PADEC_DEFAULT_LOCK);
        }
        if (s.contains(PADEC_REQUESTED_PRECISION)){
            this.requestedPrecision = s.getDouble(PADEC_REQUESTED_PRECISION);
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
        if (cryptoKeys == null){
            cryptoKeys = new LinkedHashMap<>();
        }
        super.setAppID(APP_ID);
    }

    public int getDestMin() {
        return destMin;
    }

    public int getDestMax() {
        return destMax;
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

    public int getDefaultLock() {
        return defaultLock;
    }

    public double getInterval() {
        return interval;
    }

    public double getRequestedPrecision() {
        return requestedPrecision;
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
        this.defaultLock = a.getDefaultLock();
        this.requestedPrecision = a.getRequestedPrecision();
        this.interval = a.getInterval();
        this.rng = new Random(this.seed);
    }

    private int findFittingKeyhole(List<Keyhole> keyholes, Double requestedPrecision){
        for (int i=0;i<keyholes.size();i++){
            if(keyholes.get(i).getPrecision() <= requestedPrecision){
                return i;
            }
        }
        return ACCESS_LEVEL_MAX;
    }

    private Message consumerHandle(Message msg, DTNHost host){
        if(!contexts.containsKey(host.getAddress())){ //If there is no context yet
            PADECContext cntxt = new PADECContext(); // Create it
            cntxt.registerAttribute(Location.class); // Register location
            cntxt.registerAttribute(SoundLevel.class); // Register Sound Level
            contexts.put(host.getAddress(), cntxt); // Save it
        }
        Integer type = (Integer) msg.getProperty(MSG_TYPE);
        SimpleCrypto sc = SimpleCrypto.getInstance();
        switch (type){
            case MSG_TYPE_KEYHOLE:
                byte[] encKh = (byte[]) msg.getProperty(KH_ANSW_KEYHOLE);
                List<Keyhole> keyholes = (List<Keyhole>) sc.decrypt(encKh, cryptoKeys.get(host.getAddress()).getPrivate());
                int khPos = findFittingKeyhole(keyholes, requestedPrecision);
                Keyhole kh = khPos == -1 ? keyholes.get(keyholes.size()-1) : keyholes.get(khPos);
                PADECContext cntxt = contexts.get(host.getAddress());
                cntxt.getAttribute(Location.class).setValue(
                        new Pair<>(host.getLocation().getX(), host.getLocation().getY())); // Update location
                cntxt.getAttribute(SoundLevel.class).setValue(15.0); // Update sound level
                Key key = new Key(kh, cntxt);
                String id = "k-"+host.getAddress()+"-"+msg.getFrom().getAddress()+"@"+SimClock.getIntTime();
                Message m = new Message(host, msg.getFrom(), id, 1);
                m.addProperty(MSG_TYPE, MSG_TYPE_KEY);
                m.addProperty(KEY_KEY, sc.encrypt(key, cryptoKeys.get(msg.getFrom().getAddress()).getPublic()));
                m.addProperty(KEY_ENDPOINT_PARAMS, new HashMap<>());
                m.addProperty(KEY_ACCESS_LEVEL, khPos);
                m.setAppID(APP_ID);
                super.sendEventToListeners("GotKeyhole", null, host);
                host.createNewMessage(m);
                break;
            case MSG_TYPE_INFO:
                byte[] dataEnc = (byte[]) msg.getProperty(INFO_DATA);
                FilteredData data = (FilteredData) sc.decrypt(dataEnc, cryptoKeys.get(host.getAddress()).getPrivate());
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
                if (defaultLock != LockProvider.LOCK_HISTORY) {
                    endpoints.put(host.getAddress(), new LocationEndpoint()); // Create it
                } else {
                    endpoints.put(host.getAddress(), new HistoryEndpoint("padec_history/Histo1.json"));
                }
            }
            if(!contexts.containsKey(host.getAddress())){ //If there is no context yet
                PADECContext cntxt = new PADECContext(); // Create it
                cntxt.registerAttribute(Location.class); // Register location
                cntxt.registerAttribute(SoundLevel.class); // Register Sound Level
                contexts.put(host.getAddress(), cntxt); // Save it
            }
            Lock lock = LockProvider.getLock(endpoints.get(host.getAddress()), contexts.get(host.getAddress()), defaultLock);
            locks.put(host.getAddress(), lock); // Save the access level
        }
        Integer type = (Integer) msg.getProperty(MSG_TYPE);
        SimpleCrypto sc = SimpleCrypto.getInstance();
        switch (type){
            case MSG_TYPE_KEYHOLE_REQUEST:
                List<Keyhole> keyholes = locks.get(host.getAddress()).getKeyholes();
                String id = "kha-"+host.getAddress()+"-"+msg.getFrom().getAddress()+"@"+SimClock.getIntTime();
                Message m = new Message(host, msg.getFrom(), id, 1);
                byte[] encKh = sc.encrypt(keyholes, cryptoKeys.get(msg.getFrom().getAddress()).getPublic());
                m.addProperty(MSG_TYPE, MSG_TYPE_KEYHOLE);
                m.addProperty(KH_ANSW_KEYHOLE, encKh);
                m.setAppID(APP_ID);
                host.createNewMessage(m);
                super.sendEventToListeners("GotKeyholeRequest", null, host);
                break;
            case MSG_TYPE_KEY:
                byte[] encK = (byte[]) msg.getProperty(KEY_KEY);
                Key k = (Key) sc.decrypt(encK, cryptoKeys.get(host.getAddress()).getPrivate());
                Integer al = (Integer) msg.getProperty(KEY_ACCESS_LEVEL);
                AccessLevel rAl;
                if (al == ACCESS_LEVEL_MAX){
                    rAl = locks.get(host.getAddress()).getMaxAccessLevel();
                }
                else{
                    rAl = locks.get(host.getAddress()).getAccessLevel(al);
                    rAl = rAl == null ? locks.get(host.getAddress()).getMaxAccessLevel() : rAl;
                }
                Map<String, Object> params = (Map<String, Object>) msg.getProperty(KEY_ENDPOINT_PARAMS);

                // Update location endpoint
                if (defaultLock != LockProvider.LOCK_HISTORY) {
                    ((LocationEndpoint) endpoints.get(host.getAddress())).updateLocation(
                            new Pair<>(host.getLocation().getX(), host.getLocation().getY()));
                }

                // Update location context
                contexts.get(host.getAddress()).getAttribute(Location.class).setValue(
                        new Pair<>(host.getLocation().getX(), host.getLocation().getY()));

                // Update sound level context
                contexts.get(host.getAddress()).getAttribute(SoundLevel.class).setValue(15.0);

                FilteredData result = rAl.testAccess(params, k);
                id = "resp-"+host.getAddress()+"-"+msg.getFrom().getAddress()+"@"+SimClock.getIntTime();
                m = new Message(host, msg.getFrom(), id, 1);
                if (result != null){ // Key accepted
                    m.addProperty(MSG_TYPE, MSG_TYPE_INFO);
                    byte[] encRes = sc.encrypt(result, cryptoKeys.get(msg.getFrom().getAddress()).getPublic());
                    m.addProperty(INFO_DATA, encRes);
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
        if(isConsumer()){
            if(!contexts.containsKey(host.getAddress())){ //If there is no context yet
                PADECContext cntxt = new PADECContext(); // Create it
                // Do not register a thing
                contexts.put(host.getAddress(), cntxt); // Save it
            }
            Integer type = (Integer) msg.getProperty(MSG_TYPE);
            SimpleCrypto sc = SimpleCrypto.getInstance();
            switch (type){
                case MSG_TYPE_KEYHOLE:
                    byte[] encKh = (byte[]) msg.getProperty(KH_ANSW_KEYHOLE);
                    List<Keyhole> keyholes = (List<Keyhole>) sc.decrypt(encKh, cryptoKeys.get(host.getAddress()).getPrivate());
                    int khPos = findFittingKeyhole(keyholes, requestedPrecision);
                    Keyhole kh = khPos == -1 ? keyholes.get(keyholes.size()-1) : keyholes.get(khPos);
                    PADECContext cntxt = contexts.get(host.getAddress());
                    Key key = new Key(kh, cntxt);
                    String id = "k-"+host.getAddress()+"-"+msg.getFrom().getAddress()+"@"+SimClock.getIntTime();
                    Message m = new Message(host, msg.getFrom(), id, 1);
                    m.addProperty(MSG_TYPE, MSG_TYPE_KEY);
                    m.addProperty(KEY_KEY, sc.encrypt(key, cryptoKeys.get(msg.getFrom().getAddress()).getPublic()));
                    m.addProperty(KEY_ENDPOINT_PARAMS, new HashMap<>());
                    m.addProperty(KEY_ACCESS_LEVEL, khPos);
                    m.setAppID(APP_ID);
                    super.sendEventToListeners("AttackedKeyhole", null, host);
                    host.createNewMessage(m);
                    break;
                case MSG_TYPE_INFO:
                    byte[] dataEnc = (byte[]) msg.getProperty(INFO_DATA);
                    FilteredData data = (FilteredData) sc.decrypt(dataEnc, cryptoKeys.get(host.getAddress()).getPrivate());
                    super.sendEventToListeners("AttackSuccessful", null, host);
                    lastRequest.put(host.getAddress(), -1*lastRequest.get(host.getAddress()));
                    break;
                case MSG_TYPE_DENIED:
                    super.sendEventToListeners("AttackRejected", null, host);
                    lastRequest.put(host.getAddress(), -1*lastRequest.get(host.getAddress()));
                    break;
            }
            return msg;
        }
        if(isProvider()){
            //Provider attacks
            //TODO Do we even have these? The key is not stored or accessed by underlying apps.
        }
        if(!isConsumer() && !isProvider()){
            Integer type = (Integer) msg.getProperty(MSG_TYPE);
            SimpleCrypto sc = SimpleCrypto.getInstance();
            switch (type){
                case MSG_TYPE_KEYHOLE_REQUEST:
                    //No attack to be performed
                    break;
                case MSG_TYPE_KEYHOLE:
                    super.sendEventToListeners("TPartyInfoReveal", null, host);
                    try{
                        byte[] encKh = (byte[]) msg.getProperty(KH_ANSW_KEYHOLE);
                        Keyhole kh = (Keyhole) sc.decrypt(encKh, cryptoKeys.get(host.getAddress()).getPrivate());
                        if (kh != null) {
                            super.sendEventToListeners("TPartyRevealSuccessful", null, host);
                        }
                        else{
                            super.sendEventToListeners("AttackRejected", null, host);
                        }
                    }
                    catch (Exception ex){
                        super.sendEventToListeners("AttackRejected", null, host);
                    }
                    break;
                case MSG_TYPE_INFO:
                    super.sendEventToListeners("TPartyInfoReveal", null, host);
                    try{
                        byte[] dataEnc = (byte[]) msg.getProperty(INFO_DATA);
                        FilteredData data = (FilteredData) sc.decrypt(dataEnc, cryptoKeys.get(host.getAddress()).getPrivate());
                        if (data != null) {
                            super.sendEventToListeners("TPartyRevealSuccessful", null, host);
                        }
                        else{
                            super.sendEventToListeners("AttackRejected", null, host);
                        }
                    }
                    catch (Exception ex){
                        super.sendEventToListeners("AttackRejected", null, host);
                    }
                    break;
            }
        }
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
        if(!cryptoKeys.containsKey(host.getAddress())){
            SimpleCrypto sc = SimpleCrypto.getInstance();
            cryptoKeys.put(host.getAddress(), sc.generateKeys());
        }
        Integer type = (Integer) msg.getProperty(MSG_TYPE);
        if (type == null || (msg.getTo() != host && !attacker)){
            return msg; // Not a PADEC message, or not directed to that host and host is not malicious
        }
        if (isAttacker()){
            return attackerHandle(msg, host);
        }
        if (isConsumer() && isProvider()) {
            return providerAndConsumerHandle(msg, host);
        }
        if (isConsumer()) {
            return consumerHandle(msg, host);
        }
        if (isProvider()) {
            return providerHandle(msg, host);
        }
        return msg;
    }

    /**
     * Draws a random host from the destination range
     *
     * @return host
     */
    private DTNHost randomHost() {
        int destaddr;
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
            if(!cryptoKeys.containsKey(host.getAddress())){
                SimpleCrypto sc = SimpleCrypto.getInstance();
                cryptoKeys.put(host.getAddress(), sc.generateKeys());
            }
            double curTime = SimClock.getTime();
            if (lastRequest.containsKey(host.getAddress())){ // If I have sent a request previously
                double lastReq = lastRequest.get(host.getAddress());
                if (lastReq < 0 || curTime - lastReq < this.interval) { // If I'm waiting for an answer or it is too early
                    return; // Do nothing
                }
            }
            // In any other case, send a keyhole request
            DTNHost destination = randomHost();
            if(!cryptoKeys.containsKey(destination.getAddress())){
                SimpleCrypto sc = SimpleCrypto.getInstance();
                cryptoKeys.put(destination.getAddress(), sc.generateKeys());
            }
            String id = "khr-"+host.getAddress()+"-"+destination.getAddress()+"@"+SimClock.getIntTime();
            Message m = new Message(host, destination, id, 1);
            m.addProperty(MSG_TYPE, MSG_TYPE_KEYHOLE_REQUEST);
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
