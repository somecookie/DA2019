package ch.epfl.da;

@FunctionalInterface
public interface Callback<T> {
	/**
	 * Callback method used in case of success
	 *
	 * @param value returned value from the method using the callback
	 */
	 void onSuccess(T value);

}
