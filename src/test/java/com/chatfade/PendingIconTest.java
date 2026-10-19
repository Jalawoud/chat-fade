package com.chatfade;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.IntFunction;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * An {@code <img=N>} that cannot be resolved is dropped outright, and the plain text has no
 * icon in it either, so a message whose body is only an emoji renders as a bare "name:".
 * That is #33, reported by @jarromie.
 *
 * <p>These pin the rendering half of it. The other half — noticing the icon has since been
 * registered and rebuilding the spans — needs the client and lives in the plugin.
 */
public class PendingIconTest
{
	private static final BufferedImage ICON = new BufferedImage(11, 11, BufferedImage.TYPE_INT_ARGB);

	/** Resolves nothing, standing in for a sprite array that has not been populated yet. */
	private static final IntFunction<BufferedImage> UNAVAILABLE = i -> null;
	private static final IntFunction<BufferedImage> AVAILABLE = i -> ICON;

	private static String rendered(String raw, IntFunction<BufferedImage> resolver)
	{
		List<ColorSpan> spans = ChatFadePlugin.parseColorSpans(raw, Color.WHITE, resolver);
		if (spans == null)
		{
			return ChatFadePlugin.toDisplayText(raw);
		}

		StringBuilder sb = new StringBuilder();
		spans.forEach(s -> sb.append(s.isIcon() ? "[ICON]" : s.getText()));
		return sb.toString();
	}

	@Test
	public void anEmojiOnlyBodyDisappearsWhileTheIconIsUnavailable()
	{
		// The symptom from the issue: the message renders as just the sender and a colon.
		assertEquals("pharmacopeia:", rendered("pharmacopeia: <img=1234>", UNAVAILABLE));
	}

	@Test
	public void theSameMessageKeepsItsEmojiOnceTheIconExists()
	{
		assertEquals("pharmacopeia: [ICON]", rendered("pharmacopeia: <img=1234>", AVAILABLE));
	}

	@Test
	public void theDisplayTextAloneNeverCarriesTheEmoji()
	{
		// Why the body vanishes rather than falling back to something: toDisplayText strips
		// the tag, so the spans are the only place an icon can come from.
		assertEquals("pharmacopeia:", ChatFadePlugin.toDisplayText("pharmacopeia: <img=1234>"));
	}

	@Test
	public void surroundingTextSurvivesAnUnavailableIcon()
	{
		assertEquals("Bob: hello  there", rendered("Bob: hello <img=1234> there", UNAVAILABLE));
	}

	@Test
	public void aResolvedIconIsKeptAlongsideItsText()
	{
		String out = rendered("Bob: hello <img=1234> there", AVAILABLE);

		assertTrue("the icon should be between the words: " + out, out.contains("[ICON]"));
		assertTrue(out.startsWith("Bob: hello"));
		assertTrue(out.endsWith("there"));
	}

	@Test
	public void rankBadgesAreUnaffectedByAMissingInlineIcon()
	{
		// Sender badges come from a different lookup, which is why the screenshot shows the
		// rank icon drawn while the emoji is gone.
		assertNotNull(ICON);
		assertEquals("Bob:", rendered("Bob: <img=9999>", UNAVAILABLE));
	}
}
