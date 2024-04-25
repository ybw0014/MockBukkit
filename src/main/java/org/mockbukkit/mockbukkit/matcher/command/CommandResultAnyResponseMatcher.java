package org.mockbukkit.mockbukkit.matcher.command;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.mockbukkit.mockbukkit.command.CommandResult;
import org.mockbukkit.mockbukkit.command.MessageTarget;

import java.lang.reflect.Field;

public class CommandResultAnyResponseMatcher extends TypeSafeMatcher<CommandResult>
{

	private String senderMessage = null;

	@Override
	protected boolean matchesSafely(CommandResult item)
	{
		try
		{
			Field currentItemField = item.getClass().getDeclaredField("sender");
			currentItemField.setAccessible(true);
			MessageTarget sender = (MessageTarget) currentItemField.get(item);
			senderMessage = sender.nextMessage();
			return senderMessage != null;
		}
		catch (NoSuchFieldException | IllegalAccessException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void describeTo(Description description)
	{
		description.appendText("to have any messages sent to command sender");
	}

	@Override
	protected void describeMismatchSafely(CommandResult item, Description mismatchDescription)
	{
		mismatchDescription.appendText("was value ").appendValue(senderMessage);
	}

	public static CommandResultAnyResponseMatcher hasAnyResponse()
	{
		return new CommandResultAnyResponseMatcher();
	}

}
