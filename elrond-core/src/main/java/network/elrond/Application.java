package network.elrond;

import network.elrond.application.AppContext;
import network.elrond.application.AppState;
import network.elrond.processor.AppTasks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;


public class Application implements Serializable {

    private static final Logger logger = LogManager.getLogger(Application.class);

    private AppContext context;

    private AppState state = new AppState();

    public Application(AppContext context) {
        logger.traceEntry("params: {}", context);
        if (context == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Context cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        this.context = context;
        logger.traceExit();
    }

    public AppContext getContext() {
        return context;
    }

    public void setContext(AppContext context) {
        logger.traceEntry("params: {}", context);
        if (context == null) {
            IllegalArgumentException ex = new IllegalArgumentException("Context cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        this.context = context;
        logger.traceExit();
    }

    public AppState getState() {
        return state;
    }

    public void setState(AppState state) {
        logger.traceEntry("params: {}", state);
        if (state == null) {
            IllegalArgumentException ex = new IllegalArgumentException("State cannot be null");
            logger.throwing(ex);
            throw ex;
        }
        this.state = state;
        logger.traceExit();
    }

    /**
     * Start Elrond application
     *
     * @throws IOException
     */
    public void start() throws IOException {
        logger.traceEntry();

        logger.debug("Starting private-public keys processor...");
        AppTasks.INITIALIZE_PUBLIC_PRIVATE_KEYS.process(this);

        logger.debug("Starting sharding...");
        AppTasks.INIT_SHARDING.process(this);

        logger.debug("Starting P2P communications...");
        AppTasks.INIT_P2P_CONNECTION.process(this);

        logger.debug("Starting blockchain...");
        AppTasks.INIT_BLOCKCHAIN.process(this);

        logger.debug("Starting accounts...");
        AppTasks.INIT_ACCOUNTS.process(this);

        logger.debug("Starting obect request mechanism...");
        AppTasks.INIT_REQUEST_OBJECT.process(this);

        logger.debug("Intercept P2P transactions...");
        AppTasks.INTERCEPT_TRANSACTIONS.process(this);

        logger.debug("Intercept P2P cross shard transactions...");
        AppTasks.INTERCEPT_XTRANSACTIONS.process(this);

        logger.debug("Intercept P2P receipts...");
        AppTasks.INTERCEPT_RECEIPTS.process(this);

        logger.debug("Intercept P2P blocks...");
        AppTasks.INTERCEPT_BLOCKS.process(this);

        logger.debug("Starting bootstrapping processor...");
        AppTasks.BLOCKCHAIN_BOOTSTRAP.process(this);

        logger.debug("Starting blockchain synchronization...");
        AppTasks.BLOCKCHAIN_SYNCRONIZATION.process(this);

        //logger.debug("Execute transactions and emit blocks...");
        //AppTasks.BLOCK_ASSEMBLY_PROCESSOR.process(this);


        logger.debug("Init NTP client...");
        AppTasks.NTP_CLIENT_INITIALIZER.process(this);
        
        logger.debug("Start chronology processor...");
        AppTasks.CHRONOLOGY.process(this);

        logger.debug("Start status printer...");
        AppTasks.STATUS_PRINTER.process(this);
    }

    /**
     * Stop Elrond application
     */
    public void stop() {
        this.state.setStillRunning(false);
        this.state.getConnection().getDht().shutdown();
        this.state.getConnection().getPeer().shutdown();
    }

}
