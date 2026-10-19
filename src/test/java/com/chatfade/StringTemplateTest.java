package com.chatfade;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Some broadcasts carry their variable part as a named template — a definition holding the
 * value, then a reference where it belongs — rather than writing it inline:
 *
 * <pre>&lt;str_quest_name_0=The Lost Tribe&gt;Bob has completed a quest: &lt;str_quest_name_0&gt;</pre>
 *
 * <p>Both halves look like ordinary markup, so stripping tags took the quest name with them
 * and the message arrived ending at its colon. That is #32, reported by @jarromie and
 * confirmed by @wizzyhatg.
 */
public class StringTemplateTest
{
	private static final BufferedImage ICON = new BufferedImage(11, 11, BufferedImage.TYPE_INT_ARGB);

	private static final String QUEST =
		"<str_quest_name_0=The Lost Tribe>Bob has completed a quest: <str_quest_name_0>";

	private static String expand(String raw)
	{
		return ChatFadePlugin.expandStringTemplates(raw);
	}

	/** What the overlay would actually draw, spans and all. */
	private static String rendered(String raw)
	{
		String expanded = ChatFadePlugin.stripIngestPrefixes(expand(raw));
		List<ColorSpan> spans = ChatFadePlugin.parseColorSpans(expanded, Color.WHITE, i -> ICON);
		if (spans == null)
		{
			return ChatFadePlugin.toDisplayText(expanded);
		}

		StringBuilder sb = new StringBuilder();
		spans.stream().filter(s -> !s.isIcon()).forEach(s -> sb.append(s.getText()));
		return sb.toString();
	}

	@Test
	public void keepsTheQuestName()
	{
		assertEquals("Bob has completed a quest: The Lost Tribe", rendered(QUEST));
	}

	@Test
	public void theDisplayTextAndTheSpansAgree()
	{
		// Highlight offsets are measured on one and painted onto the other.
		String expanded = ChatFadePlugin.stripIngestPrefixes(expand(QUEST));
		assertEquals(ChatFadePlugin.toDisplayText(expanded), rendered(QUEST));
	}

	@Test
	public void survivesALeadingRankIcon()
	{
		String raw = "<img=22> " + QUEST;

		assertTrue("the quest name should be there: " + rendered(raw),
			rendered(raw).endsWith("The Lost Tribe"));
	}

	@Test
	public void handlesAQuestNameContainingPunctuation()
	{
		assertEquals("Bob has completed a quest: Eagles' Peak",
			rendered("<str_quest_name_0=Eagles' Peak>Bob has completed a quest: <str_quest_name_0>"));
	}

	@Test
	public void substitutesEveryTemplateNotJustTheFirst()
	{
		assertEquals("one Alpha two Beta",
			expand("<str_a=Alpha><str_b=Beta>one <str_a> two <str_b>"));
	}

	@Test
	public void repeatsAValueUsedMoreThanOnce()
	{
		assertEquals("Alpha and Alpha", expand("<str_a=Alpha><str_a> and <str_a>"));
	}

	@Test
	public void leavesAMessageWithoutTemplatesAlone()
	{
		String plain = "<img=22> Bob has completed a quest: Cook's Assistant";

		assertEquals(plain, expand(plain));
	}

	@Test
	public void leavesColourMarkupAlone()
	{
		// <col=...> is not a template and must survive to be parsed as colour.
		String coloured = "<col=ff0000>Bob has completed a quest: Dragon Slayer</col>";

		assertEquals(coloured, expand(coloured));
	}

	@Test
	public void toleratesAReferenceWithNoDefinition()
	{
		// Nothing to substitute; the stray reference is stripped later as ordinary markup.
		assertEquals("Bob has completed a quest: <str_missing>",
			expand("Bob has completed a quest: <str_missing>"));
	}

	@Test
	public void handlesAnEmptyValue()
	{
		assertEquals("Bob has completed a quest: ",
			expand("<str_quest_name_0=>Bob has completed a quest: <str_quest_name_0>"));
	}

	@Test
	public void handlesNullAndPlainText()
	{
		assertEquals(null, expand(null));
		assertEquals("nothing special here", expand("nothing special here"));
	}
}
