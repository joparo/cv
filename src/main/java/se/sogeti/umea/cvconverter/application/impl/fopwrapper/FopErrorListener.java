package se.sogeti.umea.cvconverter.application.impl.fopwrapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.events.Event;
import org.apache.fop.events.EventFormatter;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.model.EventSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FopErrorListener implements EventListener {

	@SuppressWarnings("unused")
	private final static Logger LOG = LoggerFactory
			.getLogger(FopErrorListener.class);

	private final List<Event> events = new ArrayList<>();

	@Override
	public void processEvent(Event event) {
		EventSeverity severity = event.getSeverity();
		if (severity == EventSeverity.FATAL) {
			events.add(event);
		}		
	}

	public List<Event> getEvents() {
		List<Event> allEvents = new ArrayList<>();
		allEvents.addAll(events);
		return allEvents;
	}

	public boolean containsFatalEvents() {
		return events.size() > 0;
	}

	public String getFatalEventInfo() {
		StringBuffer fatalEventInfo = new StringBuffer();
		for (Event e : events) {
			fatalEventInfo.append("Fatal event (msg=");
			fatalEventInfo.append(EventFormatter.format(e));
			fatalEventInfo.append(") | \n");
		}
		return fatalEventInfo.toString();
	}

}
