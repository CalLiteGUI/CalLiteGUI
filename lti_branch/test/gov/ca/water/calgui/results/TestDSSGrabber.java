package gov.ca.water.calgui.results;

import hec.io.TimeSeriesContainer;

import javax.swing.JList;

import junit.framework.TestCase;

import com.limno.calgui.results.DSS_Grabber;

public class TestDSSGrabber extends TestCase {

	private DSS_Grabber grabber;

	public TestDSSGrabber() {

	}

	@Override
	protected void setUp() {
		JList list = new JList(new String[] { "item1", "item2" });
		grabber = new DSS_Grabber(list);
	}

	@Override
	protected void tearDown() {
	}

	public void testOrderOfCalls() {
		grabber.setBase("base");
		TimeSeriesContainer[] primarySeries = grabber.getPrimarySeries();
		assertNotNull(primarySeries);
		assertEquals("title", grabber.getTitle());
	}
}
