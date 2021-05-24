package applications;

import core.*;
import padec.application.Endpoint;
import padec.application.HistoryEndpoint;
import padec.application.LocationEndpoint;
import padec.attribute.Identity;
import padec.attribute.Location;
import padec.attribute.PADECContext;
import padec.attribute.SoundLevel;
import padec.crypto.SimpleCrypto;
import padec.filtering.FilteredData;
import padec.filtering.techniques.NoFilter;
import padec.key.Key;
import padec.lock.AccessLevel;
import padec.lock.Keyhole;
import padec.lock.Lock;
import padec.parser.LockParser;
import padec.perception.PrivacyPerception;
import padec.registry.ServiceRegistry;
import padec.util.Pair;

import java.security.KeyPair;
import java.util.*;

public class PADECApp extends Application {

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
    /** Defines the lock file to read **/
    public static final String PADEC_LOCK_FILE = "lock";
    /** (Minimum) interval between consumer requests **/
    public static final String PADEC_REQUEST_INTERVAL = "interval";
    /** Requested precision **/
    public static final String PADEC_REQUESTED_PRECISION = "reqprec";
    /** Perception file to read **/
    public static final String PADEC_PERCEPTION_FILE = "perception";
    /** Maximum level of info to release by consumers **/
    public static final String PADEC_RELEASE_POLICY = "policy";
    /** Endpoint to consume. */
    public static final String PADEC_SERVICE_CONSUMED = "endpoint";
    /** PADEC Step for implementation purposes */
    public static final String PADEC_STEP = "step";

    /** Key that stores the message type **/
    public static final String MSG_TYPE = "type";

    /**
     * Key that stores the requested service in a keyhole request
     **/
    public static final String KH_REQ_SERVICE = "khr-service";

    /** Key that stores the keyhole in a keyhole answer **/
    public static final String KH_ANSW_KEYHOLE = "kha-keyhole";

    /** Key that stores the key in a key message **/
    public static final String KEY_KEY = "k-key";
    /**
     * Key that stores the requested service in a key message
     **/
    public static final String KEY_REQ_SERVICE = "k-service";

    /**
     * Key that stores the minimum precision required in a key message
     **/
    public static final String KEY_MIN_PRECISION = "k-min-precision";
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
    private String lockFile = "padec_locks/HistoryLock.yaml";
    private double requestedPrecision = 0.0;
    private boolean provider = false;
    private boolean consumer = false;
    private boolean attacker = false;
    private double interval = 500;
    private int releasePolicy = 3;
    private int step = 6;
    private String perceptionFile = "padec_perceptions/PerceptA.yaml";
    private String reqEndpoint = HistoryEndpoint.class.getName();
    private Random rng;

    //Static vars for managing PADEC-specific entities
    /** Maps contexts by address. Lets the app get the context of a host, avoiding context collision between hosts **/
    private static Map<Integer, PADECContext> contexts = null;
    /** Maps crypto-keypairs by address **/
    private static Map<Integer, KeyPair> cryptoKeys = null;
    /** Maps locks by address **/
    private static Map<Integer, ServiceRegistry> locks = null;
    /** Maps endpoints by address **/
    private static Map<Integer, Endpoint> endpoints = null;
    /**
     * Maps perceptions by address
     */
    private static Map<Integer, PrivacyPerception> perceptions = null;
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
        if (s.contains(PADEC_LOCK_FILE)) {
            this.lockFile = s.getSetting(PADEC_LOCK_FILE);
        }
        if (s.contains(PADEC_REQUESTED_PRECISION)){
            this.requestedPrecision = s.getDouble(PADEC_REQUESTED_PRECISION);
        }
        if (s.contains(PADEC_REQUEST_INTERVAL)){
            this.interval = s.getDouble(PADEC_REQUEST_INTERVAL);
        }
        if (s.contains(PADEC_PERCEPTION_FILE)) {
            this.perceptionFile = s.getSetting(PADEC_PERCEPTION_FILE);
        }
        if (s.contains(PADEC_RELEASE_POLICY)) {
            this.releasePolicy = s.getInt(PADEC_RELEASE_POLICY);
        }
        if (s.contains(PADEC_SERVICE_CONSUMED)) {
            this.reqEndpoint = s.getSetting(PADEC_SERVICE_CONSUMED);
        }
        if (s.contains(PADEC_SEED)){
            this.seed = s.getInt(PADEC_SEED);
        }
        if (s.contains(PADEC_STEP)){
            this.step = s.getInt(PADEC_STEP);
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
        if (perceptions == null) {
            perceptions = new LinkedHashMap<>();
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

    public String getLockFile() {
        return lockFile;
    }

    public double getInterval() {
        return interval;
    }

    public double getRequestedPrecision() {
        return requestedPrecision;
    }

    public int getReleasePolicy() {
        return releasePolicy;
    }

    public String getPerceptionFile() {
        return perceptionFile;
    }

    public String getReqEndpoint() {
        return reqEndpoint;
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
        this.lockFile = a.getLockFile();
        this.requestedPrecision = a.getRequestedPrecision();
        this.perceptionFile = a.getPerceptionFile();
        this.releasePolicy = a.getReleasePolicy();
        this.reqEndpoint = a.getReqEndpoint();
        this.interval = a.getInterval();
        this.rng = new Random(this.seed);
    }

    private Message consumerHandle(Message msg, DTNHost host){
        if(!contexts.containsKey(host.getAddress())){ //If there is no context yet
            PADECContext cntxt = new PADECContext(); // Create it
            cntxt.registerAttribute(Location.class); // Register location
            cntxt.registerAttribute(Identity.class); // Register identity
            cntxt.registerAttribute(SoundLevel.class); // Register sound level
            contexts.put(host.getAddress(), cntxt); // Save it
        }
        if (!perceptions.containsKey(host.getAddress())) { // If perceptions were not loaded
            PrivacyPerception perception = new PrivacyPerception(4); // Create new perceptions
            perception.loadFromYamlFile(perceptionFile); // Load them from a file
            perceptions.put(host.getAddress(), perception); // Save them in the map
        }
        Integer type = (Integer) msg.getProperty(MSG_TYPE);
        SimpleCrypto sc = SimpleCrypto.getInstance();
        switch (type){
            case MSG_TYPE_KEYHOLE:
                byte[] encKh = (byte[]) msg.getProperty(KH_ANSW_KEYHOLE);
                if (encKh == null) {
                    super.sendEventToListeners("DeniedKeyhole", null, host);
                } else {
                    UUID transID = UUID.randomUUID(); // This UUID uniquely identifies the transaction for measurement
                    super.sendEventToListeners("ChooseKHStart", new LinkedHashMap<String, Object>(){
                        {{
                            put("transID", transID.toString());
                            put("time", System.currentTimeMillis());
                        }}
                    }, host);
                    List<Keyhole> keyholes = (List<Keyhole>) sc.decrypt(encKh, cryptoKeys.get(host.getAddress()).getPrivate());
                    PrivacyPerception perception = perceptions.get(host.getAddress());
                    Keyhole kh = null;
                    if (step<6) {
                        kh = keyholes.get(new java.util.Random().nextInt(keyholes.size()));
                    }
                    else{
                        for (Keyhole khEx : keyholes) {
                            if (khEx.getCategory(perception) <= releasePolicy) {
                                if (kh == null) {
                                    kh = khEx;
                                } else {
                                    kh.join(khEx);
                                }
                            }
                        }
                    }
                    if (kh == null) {
                        kh = keyholes.get(0);
                    }
                    PADECContext cntxt = contexts.get(host.getAddress());
                    cntxt.getAttribute(Location.class).setValue(
                            new Pair<>(host.getLocation().getX(), host.getLocation().getY())); // Update location
                    cntxt.getAttribute(Identity.class).setValue(host.getAddress()); // Update identity
                    if (host.getAddress() % 2 == 0) {
                        if (host.getAddress() % 4 == 0){
                            cntxt.getAttribute(SoundLevel.class).setValue(0);
                        }
                        else {
                            cntxt.getAttribute(SoundLevel.class).setValue(-15); // Update sound level
                        }
                    }
                    else{
                        cntxt.getAttribute(SoundLevel.class).setValue(15); // Update sound level
                    }
                    Key key = new Key(kh, cntxt);
                    String id = "k-" + host.getAddress() + "-" + msg.getFrom().getAddress() + "@" + SimClock.getIntTime();
                    Message m = new Message(host, msg.getFrom(), id, 1);
                    m.addProperty(MSG_TYPE, MSG_TYPE_KEY);
                    m.addProperty(KEY_KEY, sc.encrypt(key, cryptoKeys.get(msg.getFrom().getAddress()).getPublic()));
                    m.addProperty(KEY_REQ_SERVICE, reqEndpoint);
                    m.addProperty(KEY_ENDPOINT_PARAMS, new HashMap<>());
                    m.addProperty(KEY_MIN_PRECISION, requestedPrecision);
                    m.setAppID(APP_ID);
                    super.sendEventToListeners("ChooseKHEnd", new LinkedHashMap<String, Object>() {
                        {
                            {
                                put("transID", transID.toString());
                                put("time", System.currentTimeMillis());
                            }
                        }
                    }, host);
                    super.sendEventToListeners("GotKeyhole", m, host);
                    super.sendEventToListeners("KeyCategory", kh.getCategory(perception), host);
                    super.sendEventToListeners("AttributesSent", kh.getAttributes().size(), host);
                    host.createNewMessage(m);
                }
                break;
            case MSG_TYPE_INFO:
                byte[] dataEnc = (byte[]) msg.getProperty(INFO_DATA);
                FilteredData data = (FilteredData) sc.decrypt(dataEnc, cryptoKeys.get(host.getAddress()).getPrivate());
                System.out.println("Host "+host.getAddress()+": Access granted. Info: " + data.getData() + ". Precision: " + data.getPrecision());
                lastRequest.put(host.getAddress(), -1*lastRequest.get(host.getAddress()));
                super.sendEventToListeners("AccessGranted", null, host);
                super.sendEventToListeners("PrecisionGot", data.getPrecision(), host);
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
            if(!contexts.containsKey(host.getAddress())){ //If there is no context yet
                PADECContext cntxt = new PADECContext(); // Create it
                cntxt.registerAttribute(Location.class); // Register location
                cntxt.registerAttribute(Identity.class); // Register identity
                cntxt.registerAttribute(SoundLevel.class); // Register sound level
                contexts.put(host.getAddress(), cntxt); // Save it
            }
            Lock lock = LockParser.parse(lockFile, contexts.get(host.getAddress())); // Get the lock
            ServiceRegistry registry = new ServiceRegistry(); // Create a registry
            registry.addService(lock); // Register the service
            locks.put(host.getAddress(), registry); // Save the registry
            Endpoint innerEndpoint = lock.getEndpointOnlyForTheONE();
            endpoints.put(host.getAddress(), innerEndpoint);
        }
        Integer type = (Integer) msg.getProperty(MSG_TYPE);
        SimpleCrypto sc = SimpleCrypto.getInstance();
        switch (type){
            case MSG_TYPE_KEYHOLE_REQUEST:
                UUID transID = UUID.randomUUID(); // This UUID uniquely identifies the transaction for measurement
                super.sendEventToListeners("CalcKHStart", new LinkedHashMap<String, Object>() {
                    {
                        {
                            put("transID", transID.toString());
                            put("time", System.currentTimeMillis());
                        }
                    }
                }, host);
                String requestedEndpoint = (String) msg.getProperty(KH_REQ_SERVICE);
                ServiceRegistry registry = locks.get(host.getAddress());
                List<Keyhole> keyholes = new ArrayList<>();
                byte[] encKh;
                if (registry.exposesService(requestedEndpoint)) {
                    keyholes = registry.getService(requestedEndpoint).getKeyholes();
                    encKh = sc.encrypt(keyholes, cryptoKeys.get(msg.getFrom().getAddress()).getPublic());
                } else {
                    encKh = null;
                }
                String id = "kha-" + host.getAddress() + "-" + msg.getFrom().getAddress() + "@" + SimClock.getIntTime();
                Message m = new Message(host, msg.getFrom(), id, 1);
                m.addProperty(MSG_TYPE, MSG_TYPE_KEYHOLE);
                m.addProperty(KH_ANSW_KEYHOLE, encKh);
                m.setAppID(APP_ID);
                super.sendEventToListeners("CalcKHEnd", new LinkedHashMap<String, Object>() {
                    {
                        {
                            put("transID", transID.toString());
                            put("time", System.currentTimeMillis());
                        }
                    }
                }, host);
                if (encKh != null){
                    Keyhole unitedKh = keyholes.get(0);
                    for (Keyhole kh : keyholes){
                        unitedKh.join(kh);
                    }
                    if (unitedKh.getAttributes().size() == 1 && unitedKh.getAttributes().get(0).equals(Identity.class)){
                        super.sendEventToListeners("CircumventingConstraintsAttack", null, host);
                    }
                }
                host.createNewMessage(m);
                super.sendEventToListeners("GotKeyholeRequest", m, host);
                break;
            case MSG_TYPE_KEY:
                byte[] encK = (byte[]) msg.getProperty(KEY_KEY);
                transID = UUID.randomUUID(); // This UUID uniquely identifies the transaction for measurement
                super.sendEventToListeners("RuleEvalStart", new LinkedHashMap<String, Object>() {
                    {
                        {
                            put("transID", transID.toString());
                            put("time", System.currentTimeMillis());
                        }
                    }
                }, host);
                Key k = (Key) sc.decrypt(encK, cryptoKeys.get(host.getAddress()).getPrivate());
                Double minPrec = (Double) msg.getProperty(KEY_MIN_PRECISION);
                Lock lock = locks.get(host.getAddress()).getService((String) msg.getProperty(KEY_REQ_SERVICE));
                Map<String, Object> params = (Map<String, Object>) msg.getProperty(KEY_ENDPOINT_PARAMS);
                // Update location endpoint
                Endpoint endpoint = endpoints.get(host.getAddress());
                if (endpoint instanceof LocationEndpoint) {
                    ((LocationEndpoint) endpoints.get(host.getAddress())).updateLocation(
                            new Pair<>(host.getLocation().getX(), host.getLocation().getY()));
                }
                // Update history endpoint
                if (endpoint instanceof HistoryEndpoint) {
                    HistoryEndpoint he = (HistoryEndpoint) endpoints.get(host.getAddress());
                    if (!he.isLoaded()) {
                        he.load("padec_history/Histo" + host.getAddress() + ".json");
                    }
                }

                // Update location context
                contexts.get(host.getAddress()).getAttribute(Location.class).setValue(
                        new Pair<>(host.getLocation().getX(), host.getLocation().getY()));

                // Update identity context
                contexts.get(host.getAddress()).getAttribute(Identity.class).setValue(host.getAddress());

                // Update sound level context
                contexts.get(host.getAddress()).getAttribute(SoundLevel.class).setValue(-15);

                // Keyhole lookup
                List<Keyhole> khLookup = lock.getKeyholes();
                FilteredData result = null;
                boolean fitting = false;
                for (int i = khLookup.size() - 1; i >= 0; i--) {
                    Keyhole kh = khLookup.get(i);
                    if (kh.getPrecision() > minPrec) { // If this keyhole is too bad for the requestor
                        if (!fitting) {
                            super.sendEventToListeners("NoFittingLevel", null, host);
                        }
                        break; // No need to keep on looking
                    } else {
                        if (kh.fits(k)) { //If the key fits
                            fitting = true;
                            AccessLevel al = lock.getAccessLevel(i);
                            super.sendEventToListeners("FittingLevel", new int[]{i, msg.getFrom().getAddress()}, host);
                            if(al.getFilterKind().equals(NoFilter.class)){
                                super.sendEventToListeners("InsiderAttack", null, host);
                            }
                            result = al.testAccess(params, k); // Test access
                            if (result != null) { // If access is granted
                                super.sendEventToListeners("ReqProvPrec", new Pair<Double, Double>(minPrec, kh.getPrecision()), host); // Report
                                break; // No need to keep on looking
                            }
                        }
                    }
                }

                id = "resp-"+host.getAddress()+"-"+msg.getFrom().getAddress()+"@"+SimClock.getIntTime();
                m = new Message(host, msg.getFrom(), id, 1);
                if (result != null){ // Key accepted
                    m.addProperty(MSG_TYPE, MSG_TYPE_INFO);
                    Double totalSize = (double) ((HistoryEndpoint) endpoints.get(host.getAddress())).execute(params).size();
                    Double sentSize = (double) ((List) result.getData()).size();
                    super.sendEventToListeners("InfSentMetric", sentSize / totalSize, host);
                    byte[] encRes = sc.encrypt(result, cryptoKeys.get(msg.getFrom().getAddress()).getPublic());
                    m.addProperty(INFO_DATA, encRes);
                }
                else{ // Key rejected
                    m.addProperty(MSG_TYPE, MSG_TYPE_DENIED);
                }
                m.setAppID(APP_ID);
                super.sendEventToListeners("RuleEvalEnd", new LinkedHashMap<String, Object>() {
                    {
                        {
                            put("transID", transID.toString());
                            put("time", System.currentTimeMillis());
                        }
                    }
                }, host);
                host.createNewMessage(m);
                super.sendEventToListeners("GotKey", m, host);
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
            if (!perceptions.containsKey(host.getAddress())) { // If perceptions were not loaded
                PrivacyPerception perception = new PrivacyPerception(4); // Create new perceptions
                perception.loadFromYamlFile(perceptionFile); // Load them from a file
                perceptions.put(host.getAddress(), perception); // Save them in the map
            }
            Integer type = (Integer) msg.getProperty(MSG_TYPE);
            SimpleCrypto sc = SimpleCrypto.getInstance();
            switch (type){
                case MSG_TYPE_KEYHOLE:
                    byte[] encKh = (byte[]) msg.getProperty(KH_ANSW_KEYHOLE);
                    if (encKh != null) {
                        try {
                            List<Keyhole> keyholes = (List<Keyhole>) sc.decrypt(encKh, cryptoKeys.get(host.getAddress()).getPrivate());
                            PrivacyPerception perception = perceptions.get(host.getAddress());
                            Keyhole kh = null;
                            for (int i = keyholes.size() - 1; i >= 0; i--) {
                                Keyhole khEx = keyholes.get(i);
                                if (khEx.getCategory(perception) <= releasePolicy) {
                                    kh = khEx;
                                    break;
                                }
                            }
                            if (kh == null) {
                                kh = keyholes.get(0);
                            }
                            PADECContext cntxt = contexts.get(host.getAddress());
                            Key key = new Key(kh, cntxt);
                            String id = "k-" + host.getAddress() + "-" + msg.getFrom().getAddress() + "@" + SimClock.getIntTime();
                            Message m = new Message(host, msg.getFrom(), id, 1);
                            m.addProperty(MSG_TYPE, MSG_TYPE_KEY);
                            m.addProperty(KEY_KEY, sc.encrypt(key, cryptoKeys.get(msg.getFrom().getAddress()).getPublic()));
                            m.addProperty(KEY_REQ_SERVICE, reqEndpoint);
                            m.addProperty(KEY_ENDPOINT_PARAMS, new HashMap<>());
                            m.addProperty(KEY_MIN_PRECISION, requestedPrecision);
                            m.setAppID(APP_ID);
                            host.createNewMessage(m);
                            super.sendEventToListeners("AttackedKeyhole", m, host);
                        } catch (Exception ex) {
                            String id = "k-" + host.getAddress() + "-" + msg.getFrom().getAddress() + "@" + SimClock.getIntTime();
                            Message m = new Message(host, msg.getFrom(), id, 1);
                            super.sendEventToListeners("AttackedKeyhole", m, host);
                            super.sendEventToListeners("AttackRejected", null, host);
                        }
                    }
                    break;
                case MSG_TYPE_INFO:
                    byte[] dataEnc = (byte[]) msg.getProperty(INFO_DATA);
                    try {
                        FilteredData data = (FilteredData) sc.decrypt(dataEnc, cryptoKeys.get(host.getAddress()).getPrivate());
                        super.sendEventToListeners("AttackSuccessful", null, host);
                    } catch (Exception ex) {
                        super.sendEventToListeners("AttackRejected", null, host);
                    }
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
            m.addProperty(KH_REQ_SERVICE, reqEndpoint);
            m.setAppID(APP_ID);
            super.sendEventToListeners("PADECRequest", m, host);
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

    public static Map<Integer, KeyPair> getCryptoKeys() {
        return cryptoKeys;
    }
}
