package org.jaybaws.metrics.bw.workers;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;
import java.util.HashMap;
import java.util.Map;
import org.jaybaws.metrics.bw.util.Logger;

public class GetProcessDefinitionsWorker implements Runnable {

    private final MBeanServerConnection mbsc;
    private final ObjectName objectName;

    private final Meter meter;

    private final Map<String, Long> metrics = new HashMap<String, Long>();

    public GetProcessDefinitionsWorker(OpenTelemetry sdk, MBeanServerConnection mbsc, ObjectName objectName) {
        this.mbsc = mbsc;
        this.objectName = objectName;
        this.meter = sdk.getMeter("com.tibco.bw.hawkmethod.getprocessdefinitions");
    }

    private void trackMetric(String metricName, String processDefinitionName, long value) {
        String uniqueId = processDefinitionName + "/" + metricName;

        if (metrics.containsKey(uniqueId)) {
            metrics.replace(uniqueId, value);
        } else {
            metrics.put(uniqueId, value);
            this.meter
                    .gaugeBuilder(metricName)
                    .ofLongs()
                    .buildWithCallback(
                            result -> result.record(
                                    this.metrics.get(uniqueId),
                                    Attributes.builder()
                                            .put("process", processDefinitionName)
                                            .build()
                            )
                    );
        }
    }

    @Override
    public void run() {
        Logger.entering(this.getClass().getCanonicalName(), "run");

        try {
            TabularDataSupport result = (TabularDataSupport) mbsc.invoke(objectName, "GetProcessDefinitions", null, null);

            if (result != null) {
                for (Object value : result.values()) {
                    CompositeDataSupport resultItem = (CompositeDataSupport) value;

                    String process = (String) resultItem.get("Name");

                    trackMetric("bwengine.processdefinition.created", process, (Long) resultItem.get("Created"));
                    trackMetric("bwengine.processdefinition.suspended", process, (Long) resultItem.get("Suspended"));
                    trackMetric("bwengine.processdefinition.swapped", process, (Long) resultItem.get("Swapped"));
                    trackMetric("bwengine.processdefinition.queued", process, (Long) resultItem.get("Queued"));
                    trackMetric("bwengine.processdefinition.aborted", process, (Long) resultItem.get("Aborted"));
                    trackMetric("bwengine.processdefinition.completed", process, (Long) resultItem.get("Completed"));
                    trackMetric("bwengine.processdefinition.checkpointed", process, (Long) resultItem.get("Checkpointed"));
                    trackMetric("bwengine.processdefinition.execution_total", process, (Long) resultItem.get("TotalExecution"));
                    trackMetric("bwengine.processdefinition.execution_avg", process, (Long) resultItem.get("AverageExecution"));
                    trackMetric("bwengine.processdefinition.elapsed_total", process, (Long) resultItem.get("TotalElapsed"));
                    trackMetric("bwengine.processdefinition.elapsed_avg", process, (Long) resultItem.get("AverageElapsed"));
                    trackMetric("bwengine.processdefinition.elapsed_min", process, (Long) resultItem.get("MinElapsed"));
                    trackMetric("bwengine.processdefinition.elapsed_max", process, (Long) resultItem.get("MaxElapsed"));
                    trackMetric("bwengine.processdefinition.execution_min", process, (Long) resultItem.get("MinExecution"));
                    trackMetric("bwengine.processdefinition.execution_max", process, (Long) resultItem.get("MaxExecution"));
                    trackMetric("bwengine.processdefinition.execution_recent", process, (Long) resultItem.get("MostRecentExecutionTime"));
                    trackMetric("bwengine.processdefinition.elapsed_recent", process, (Long) resultItem.get("MostRecentElapsedTime"));
                }
            }
        } catch (Throwable t) {
            Logger.warning("Exception invoking 'GetProcessDefinitions'...", t);
        }

        Logger.exiting(this.getClass().getCanonicalName(), "run");
    }
}

/*

GetProcessDefinitions:

javax.management.openmbean.TabularDataSupport(tabularType=javax.management.openmbean.TabularType(name=GetProcessDefinitions,rowType=javax.management.openmbean.CompositeType(name=GetProcessDefinitions,items=((itemName=Aborted,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Checkpointed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Completed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=CountSinceReset,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Created,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentElapsedTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentExecutionTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Queued,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Starter,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Suspended,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Swapped,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TimeSinceLastUpdate,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)))),indexNames=(Name)),contents={[Common/Logging/Processes/DEBUG.process]=javax.management.openmbean.CompositeDataSupport(compositeType=javax.management.openmbean.CompositeType(name=GetProcessDefinitions,items=((itemName=Aborted,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Checkpointed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Completed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=CountSinceReset,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Created,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentElapsedTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentExecutionTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Queued,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Starter,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Suspended,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Swapped,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TimeSinceLastUpdate,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)))),contents={Aborted=0, AverageElapsed=3, AverageExecution=3, Checkpointed=0, Completed=1, CountSinceReset=1, Created=1, MaxElapsed=3, MaxExecution=3, MinElapsed=3, MinExecution=3, MostRecentElapsedTime=3, MostRecentExecutionTime=3, Name=Common/Logging/Processes/DEBUG.process, Queued=0, Starter=, Suspended=0, Swapped=0, TimeSinceLastUpdate=7903, TotalElapsed=3, TotalExecution=3}), [Starters/EMSSender.process]=javax.management.openmbean.CompositeDataSupport(compositeType=javax.management.openmbean.CompositeType(name=GetProcessDefinitions,items=((itemName=Aborted,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Checkpointed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Completed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=CountSinceReset,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Created,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentElapsedTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentExecutionTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Queued,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Starter,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Suspended,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Swapped,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TimeSinceLastUpdate,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)))),contents={Aborted=0, AverageElapsed=136, AverageExecution=136, Checkpointed=0, Completed=1, CountSinceReset=1, Created=1, MaxElapsed=136, MaxExecution=136, MinElapsed=136, MinExecution=136, MostRecentElapsedTime=136, MostRecentExecutionTime=136, Name=Starters/EMSSender.process, Queued=0, Starter=Timer, Suspended=0, Swapped=0, TimeSinceLastUpdate=9153, TotalElapsed=136, TotalExecution=136}), [Starters/MQSender.process]=javax.management.openmbean.CompositeDataSupport(compositeType=javax.management.openmbean.CompositeType(name=GetProcessDefinitions,items=((itemName=Aborted,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Checkpointed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Completed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=CountSinceReset,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Created,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentElapsedTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentExecutionTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Queued,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Starter,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Suspended,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Swapped,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TimeSinceLastUpdate,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)))),contents={Aborted=0, AverageElapsed=122, AverageExecution=120, Checkpointed=0, Completed=1, CountSinceReset=1, Created=1, MaxElapsed=122, MaxExecution=120, MinElapsed=122, MinExecution=120, MostRecentElapsedTime=122, MostRecentExecutionTime=120, Name=Starters/MQSender.process, Queued=0, Starter=Timer, Suspended=0, Swapped=0, TimeSinceLastUpdate=9168, TotalElapsed=122, TotalExecution=120}), [Common/Logging/Processes/ERROR.process]=javax.management.openmbean.CompositeDataSupport(compositeType=javax.management.openmbean.CompositeType(name=GetProcessDefinitions,items=((itemName=Aborted,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Checkpointed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Completed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=CountSinceReset,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Created,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentElapsedTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentExecutionTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Queued,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Starter,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Suspended,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Swapped,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TimeSinceLastUpdate,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)))),contents={Aborted=0, AverageElapsed=0, AverageExecution=0, Checkpointed=0, Completed=0, CountSinceReset=0, Created=0, MaxElapsed=0, MaxExecution=0, MinElapsed=0, MinExecution=0, MostRecentElapsedTime=0, MostRecentExecutionTime=0, Name=Common/Logging/Processes/ERROR.process, Queued=0, Starter=, Suspended=0, Swapped=0, TimeSinceLastUpdate=0, TotalElapsed=0, TotalExecution=0}), [Starters/EMSReceiver.process]=javax.management.openmbean.CompositeDataSupport(compositeType=javax.management.openmbean.CompositeType(name=GetProcessDefinitions,items=((itemName=Aborted,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Checkpointed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Completed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=CountSinceReset,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Created,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentElapsedTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentExecutionTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Queued,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Starter,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Suspended,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Swapped,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TimeSinceLastUpdate,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)))),contents={Aborted=0, AverageElapsed=19, AverageExecution=1, Checkpointed=0, Completed=1, CountSinceReset=1, Created=1, MaxElapsed=19, MaxExecution=1, MinElapsed=19, MinExecution=1, MostRecentElapsedTime=19, MostRecentExecutionTime=1, Name=Starters/EMSReceiver.process, Queued=0, Starter=JMS Queue Receiver, Suspended=0, Swapped=0, TimeSinceLastUpdate=9082, TotalElapsed=19, TotalExecution=1}), [Common/Logging/Processes/INFO.process]=javax.management.openmbean.CompositeDataSupport(compositeType=javax.management.openmbean.CompositeType(name=GetProcessDefinitions,items=((itemName=Aborted,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Checkpointed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Completed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=CountSinceReset,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Created,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentElapsedTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentExecutionTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Queued,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Starter,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Suspended,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Swapped,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TimeSinceLastUpdate,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)))),contents={Aborted=0, AverageElapsed=174, AverageExecution=172, Checkpointed=0, Completed=1, CountSinceReset=1, Created=1, MaxElapsed=174, MaxExecution=172, MinElapsed=174, MinExecution=172, MostRecentElapsedTime=174, MostRecentExecutionTime=172, Name=Common/Logging/Processes/INFO.process, Queued=0, Starter=, Suspended=0, Swapped=0, TimeSinceLastUpdate=9111, TotalElapsed=174, TotalExecution=172}), [Starters/JDBCDigger.process]=javax.management.openmbean.CompositeDataSupport(compositeType=javax.management.openmbean.CompositeType(name=GetProcessDefinitions,items=((itemName=Aborted,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Checkpointed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Completed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=CountSinceReset,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Created,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentElapsedTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentExecutionTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Queued,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Starter,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Suspended,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Swapped,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TimeSinceLastUpdate,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)))),contents={Aborted=0, AverageElapsed=1390, AverageExecution=1387, Checkpointed=0, Completed=1, CountSinceReset=1, Created=1, MaxElapsed=1390, MaxExecution=1387, MinElapsed=1390, MinExecution=1387, MostRecentElapsedTime=1390, MostRecentExecutionTime=1387, Name=Starters/JDBCDigger.process, Queued=0, Starter=Timer, Suspended=0, Swapped=0, TimeSinceLastUpdate=7902, TotalElapsed=1390, TotalExecution=1387}), [Starters/MQReceiver.process]=javax.management.openmbean.CompositeDataSupport(compositeType=javax.management.openmbean.CompositeType(name=GetProcessDefinitions,items=((itemName=Aborted,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Checkpointed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Completed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=CountSinceReset,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Created,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentElapsedTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentExecutionTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Queued,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Starter,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Suspended,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Swapped,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TimeSinceLastUpdate,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)))),contents={Aborted=0, AverageElapsed=18, AverageExecution=4, Checkpointed=0, Completed=1, CountSinceReset=1, Created=1, MaxElapsed=18, MaxExecution=4, MinElapsed=18, MinExecution=4, MostRecentElapsedTime=18, MostRecentExecutionTime=4, Name=Starters/MQReceiver.process, Queued=0, Starter=MQ Queue Receiver, Suspended=0, Swapped=0, TimeSinceLastUpdate=9147, TotalElapsed=18, TotalExecution=4}), [Common/Logging/Processes/internal/log.process]=javax.management.openmbean.CompositeDataSupport(compositeType=javax.management.openmbean.CompositeType(name=GetProcessDefinitions,items=((itemName=Aborted,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=AverageExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Checkpointed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Completed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=CountSinceReset,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Created,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MaxExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MinExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentElapsedTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=MostRecentExecutionTime,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Name,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Queued,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Starter,itemType=javax.management.openmbean.SimpleType(name=java.lang.String)),(itemName=Suspended,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=Swapped,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TimeSinceLastUpdate,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalElapsed,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)),(itemName=TotalExecution,itemType=javax.management.openmbean.SimpleType(name=java.lang.Long)))),contents={Aborted=0, AverageElapsed=71, AverageExecution=70, Checkpointed=0, Completed=2, CountSinceReset=2, Created=2, MaxElapsed=139, MaxExecution=137, MinElapsed=3, MinExecution=3, MostRecentElapsedTime=3, MostRecentExecutionTime=3, Name=Common/Logging/Processes/internal/log.process, Queued=0, Starter=, Suspended=0, Swapped=0, TimeSinceLastUpdate=7903, TotalElapsed=142, TotalExecution=140})})

 */

/*
Method: GetProcessDefinitions

Timeout(millisecs): 10000

Description: Gets process definitions

Type: Open, Synchronous, IMPACT_INFO

	Arguments: None

	Returns:
		name: return
		type: COM.TIBCO.hawk.talon.TabularData
		description: None
		isOpen: true
		elements:
			name: Name
			type: java.lang.String
			description: Process definition name
			isOpen: true
			name: Starter
			type: java.lang.String
			description: Name of this process definition's starter activity
			isOpen: true
			name: Created
			type: java.lang.Long
			description: Number of processes created for this process definition
			isOpen: true
			name: Suspended
			type: java.lang.Long
			description: Number of times processes using this process definition have been suspended
			isOpen: true
			name: Swapped
			type: java.lang.Long
			description: Number of times swapped
			isOpen: true
			name: Queued
			type: java.lang.Long
			description: Number of times queued
			isOpen: true
			name: Aborted
			type: java.lang.Long
			description: Number of times aborted
			isOpen: true
			name: Completed
			type: java.lang.Long
			description: Number of times completed
			isOpen: true
			name: Checkpointed
			type: java.lang.Long
			description: Number of times checkpointed
			isOpen: true
			name: TotalExecution
			type: java.lang.Long
			description: Total execution time of all processes completed using this process definition (milliseconds)
			isOpen: true
			name: AverageExecution
			type: java.lang.Long
			description: Average execution time of all processes completed using this process definition (milliseconds)
			isOpen: true
			name: TotalElapsed
			type: java.lang.Long
			description: Total elapsed time of all processes completed using this process definition (milliseconds)
			isOpen: true
			name: AverageElapsed
			type: java.lang.Long
			description: Average elapsed time of all processes completed using this process definition (milliseconds)
			isOpen: true
			name: MinElapsed
			type: java.lang.Long
			description: Minimum elapsed time of all processes completed using this process definition (milliseconds)
			isOpen: true
			name: MaxElapsed
			type: java.lang.Long
			description: Maximum elapsed time of all processes completed using this process definition (milliseconds)
			isOpen: true
			name: MinExecution
			type: java.lang.Long
			description: Minimum execution time of all processes completed using this process definition (milliseconds)
			isOpen: true
			name: MaxExecution
			type: java.lang.Long
			description: Maximum execution time of all processes completed using this process definition (milliseconds)
			isOpen: true
			name: MostRecentExecutionTime
			type: java.lang.Long
			description: Most recent ExecutionTime (milliseconds).
			isOpen: true
			name: MostRecentElapsedTime
			type: java.lang.Long
			description: Most recent ElapsedTime (milliseconds).
			isOpen: true
			name: TimeSinceLastUpdate
			type: java.lang.Long
			description: Time (milliseconds) since most recent values updated.
			isOpen: true
			name: CountSinceReset
			type: java.lang.Long
			description: Processes completed since last reset
			isOpen: true
		columns:
		columns: [Name, Starter, Created, Suspended, Swapped, Queued, Aborted, Completed, Checkpointed, TotalExecution, AverageExecution, TotalElapsed, AverageElapsed, MinElapsed, MaxElapsed, MinExecution, MaxExecution, MostRecentExecutionTime, MostRecentElapsedTime, TimeSinceLastUpdate, CountSinceReset]
		indexColumns: [Name]
 */