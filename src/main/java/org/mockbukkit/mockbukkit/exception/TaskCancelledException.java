package org.mockbukkit.mockbukkit.exception;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;

/**
 * Thrown when a task is force-cancelled.
 */
public class TaskCancelledException extends RuntimeException
{

	@Serial
	private static final long serialVersionUID = -45010596922351477L;

	/**
	 * Constructs a new runtime exception with the specified detail message.
	 *
	 * @param message The detail message.
	 */
	public TaskCancelledException(String message)
	{
		super(message);
	}

	/**
	 * Constructs a new runtime exception with the specified cause and a detail message.
	 *
	 * @param message The detail message.
	 * @param cause   The cause of the exception.
	 */
	public TaskCancelledException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new runtime exception with the specified cause and a detail message of (cause==null ? null : cause.toString())
	 *
	 * @param cause The cause of the exception.
	 */
	public TaskCancelledException(@NotNull Throwable cause)
	{
		super(cause);
	}

}
