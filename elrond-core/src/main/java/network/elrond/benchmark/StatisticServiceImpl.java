package network.elrond.benchmark;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class StatisticServiceImpl implements StatisticService {
    private static final Logger logger = LogManager.getLogger(StatisticServiceImpl.class);

    private static final int maxStatistics = 100;
    private List<Statistic> statistics = new ArrayList<>();

    private long currentIndex = 0;
    private long currentMillis = 0;
    private long startMillis = 0;

    private Double averageTps = 0.0;
    private Double maxTps = 0.0;
    private Double minTps = Double.MAX_VALUE;
    private Double liveTps = 0.0;

    private long averageNrTransactionsInBlock = 0;
    private long maxNrTransactionsInBlock = 0;
    private long minNrTransactionsInBlock = Integer.MAX_VALUE;
    private long liveNrTransactionsInBlock = 0;

    private long averageRoundTime = 0;
    private long liveRoundTime = 0;

    private long totalProcessedTransactions = 0;

    public StatisticServiceImpl(long startMillis){
        this.startMillis = startMillis;
        currentMillis = startMillis;
    }

    @Override
    public void addStatistic(Statistic statistic) {
        logger.traceEntry("params: {}", statistic);

        long ellapsedMillis = statistic.getCurrentTimeMillis() - currentMillis;
        liveRoundTime = ellapsedMillis;
        currentMillis = statistic.getCurrentTimeMillis();

        statistics.add(statistic);
        if (statistics.size() > maxStatistics){
            statistics.remove(0);
        }

        totalProcessedTransactions += statistic.getNrTransactionsInBlock();
        liveTps  = statistic.getNrTransactionsInBlock() * 1000.0 / ellapsedMillis;
        logger.trace("currentTps is " + liveTps);
        ComputeTps(liveTps);

        liveNrTransactionsInBlock = statistic.getNrTransactionsInBlock();
        computeNrTransactionsInBlock(liveNrTransactionsInBlock);

        computeAverageRoundTime(ellapsedMillis);

        currentIndex++;
        logger.traceExit();
    }

    private void computeAverageRoundTime(long timeDifference) {
        averageRoundTime = (averageRoundTime *currentIndex + timeDifference) / (currentIndex+1);
        logger.trace("averageNrTransactionsInBlock is " + averageNrTransactionsInBlock);
    }

    private void ComputeTps(Double currentTps) {
        if(maxTps <currentTps){
            maxTps = currentTps;
        }

        if(minTps > currentTps){
            minTps = currentTps;
        }

        averageTps = (totalProcessedTransactions * 1000.0) / (System.currentTimeMillis() - startMillis);
        logger.trace("averageTps is " + averageTps);
    }

    private void computeNrTransactionsInBlock(long currentNrTransactionsInBlock) {
        if(maxNrTransactionsInBlock <currentNrTransactionsInBlock){
            maxNrTransactionsInBlock = currentNrTransactionsInBlock;
        }

        if(minNrTransactionsInBlock > currentNrTransactionsInBlock){
            minNrTransactionsInBlock = currentNrTransactionsInBlock;
        }

        averageNrTransactionsInBlock = (averageNrTransactionsInBlock *currentIndex + currentNrTransactionsInBlock) /(currentIndex+1);
        logger.trace("averageNrTransactionsInBlock is " + averageNrTransactionsInBlock);
    }

    @Override
    public Double getAverageTps() {
        return averageTps;
    }

    @Override
    public Double getMaxTps() {
        return maxTps;
    }

    @Override
    public Double getMinTps() {
        return minTps;
    }

    @Override
    public Double getLiveTps() {
        return liveTps;
    }

    @Override
    public long getAverageNrTransactionsInBlock() {
        return averageNrTransactionsInBlock;
    }

    @Override
    public long getMaxNrTransactionsInBlock() {
        return maxNrTransactionsInBlock;
    }

    @Override
    public long getMinNrTransactionsInBlock() {
        return minNrTransactionsInBlock;
    }

    @Override
    public long getLiveNrTransactionsInBlock() {
        return liveNrTransactionsInBlock;
    }

    @Override
    public long getAverageRoundTime() {
        return averageRoundTime;
    }

    @Override
    public long getLiveRoundTime() {
        return liveRoundTime;
    }

    @Override
    public long getTotalNrProcessedTransactions() {
        return totalProcessedTransactions;
    }
}