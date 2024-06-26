// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.exceptions;

/**
 * Exception that occurrs when a task's timeout is reached.
 *
 * @author Julien Leblay
 */
public class LimitReachedException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -315472460756924167L;

	/**
	 * 
	 */
	public static enum Reasons {
		/** The timeout. */
		TIMEOUT, 
		/** The max iteration. */
		MAX_ITERATION}

	/**  */
	private final Reasons reason;

	/**
	 * Instantiates a new limit reached exception.
	 *
	 * @param msg exception message.
	 * @param reason Reasons
	 */
	public LimitReachedException(String msg, Reasons reason) {
		super(msg);
		this.reason = reason;
	}

	/**
	 * Instantiates a new limit reached exception.
	 *
	 * @param reason Reasons
	 */
	public LimitReachedException(Reasons reason) {
		super();
		this.reason = reason;
	}

	/**
	 * Gets the reason for this exception.
	 *
	 * @return Reasons
	 */
	public Reasons getReason() {
		return this.reason;
	}
}
