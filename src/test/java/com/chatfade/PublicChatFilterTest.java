package com.chatfade;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The Public chat tab has four states — On, Friends, Off and Hide — and only "Off" used to
 * be honoured, so setting Public to Hide silenced the chatbox while every passing player kept
 * fading in through the overlay.
 *
 * <p>The decision is pure given the state and whether the sender is someone "Friends" keeps,
 * so it is tested directly; reading the state off the tab widget stays in the plugin.
 */
public class PublicChatFilterTest
{
	private static boolean allowed(String state, boolean selfOrFriend)
	{
		return ChatFadePlugin.allowedByPublicFilter(state, selfOrFriend);
	}

	@Test
	public void showsEverythingWhenPublicChatIsOn()
	{
		assertTrue(allowed("On", false));
		assertTrue(allowed("On", true));
	}

	@Test
	public void hidesEverythingWhenPublicChatIsOff()
	{
		assertFalse(allowed("Off", false));
		assertFalse("Off hides your friends too", allowed("Off", true));
	}

	@Test
	public void hidesEverythingWhenPublicChatIsHidden()
	{
		// The state this was all reported for.
		assertFalse(allowed("Hide", false));
		assertFalse("Hide hides your friends too", allowed("Hide", true));
	}

	@Test
	public void keepsOnlyFriendsWhenPublicChatIsSetToFriends()
	{
		assertTrue(allowed("Friends", true));
		assertFalse(allowed("Friends", false));
	}

	@Test
	public void showsTheMessageWhenTheStateCannotBeRead()
	{
		// The state comes from widget text, so it can legitimately be missing — before the
		// chatbox is built, for one. Going quiet then would hide public chat with no cause
		// the user could see.
		assertTrue(allowed(null, false));
	}

	@Test
	public void showsTheMessageOnAnUnrecognisedState()
	{
		assertTrue(allowed("Something New", false));
	}
}
