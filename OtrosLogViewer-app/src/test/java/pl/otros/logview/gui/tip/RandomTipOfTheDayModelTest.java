package pl.otros.logview.gui.tip;

import org.jdesktop.swingx.tips.DefaultTip;
import org.jdesktop.swingx.tips.DefaultTipOfTheDayModel;
import org.jdesktop.swingx.tips.TipOfTheDayModel;
import org.jdesktop.swingx.tips.TipOfTheDayModel.Tip;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class RandomTipOfTheDayModelTest {

	private static final int TIPS_SIZE = 1000;
	RandomTipOfTheDayModel tipsModel = null;

	@Before
	public void prepare() {
		ArrayList<Tip> tips = new ArrayList<TipOfTheDayModel.Tip>();
		for (int i = 0; i < TIPS_SIZE; i++) {
			tips.add(new DefaultTip(Integer.toString(i), Integer.toString(i)));

		}
		TipOfTheDayModel model = new DefaultTipOfTheDayModel(tips);
		tipsModel = new RandomTipOfTheDayModel(model);
	}

	@Test
	public void testGetTipAtNotNull() {
		for (int i = 0; i < tipsModel.getTipCount(); i++) {
			assertNotNull(tipsModel.getTipAt(i));
		}
	}

	@Test
	public void testGetTipAtShuffled() {
		boolean diffrence = false;
		for (int i = 0; i < tipsModel.getTipCount(); i++) {
			if (tipsModel.getTipAt(i).getTipName() != Integer.toString(i)) {
				diffrence=true;
			}			
		}
		assertTrue(diffrence);
	}

	@Test
	public void testGetTipCount() {
		assertEquals(TIPS_SIZE, tipsModel.getTipCount());
	}

}
