package black;

import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

/** Checks TypedString stays an empty marker, and that the example in its Javadoc works; exits non-zero on a failure. */
public final class TypedStringTest {
	private static int failures;
	private static int checks;

	private TypedStringTest() {}

	/** The example from TypedString's Javadoc, as written there. */
	record Sku(String value) implements TypedString {
		private static final Pattern SHAPE = Pattern.compile("[A-Z]{3}-\\d{6}");

		Sku {
			if (!SHAPE.matcher(value).matches()) {
				throw new IllegalArgumentException("not a SKU: " + value);
			}
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public static void main(String[] args) {
		// A marker: adding anything to it would break every implementation that doesn't have it.
		Class<TypedString> marker = TypedString.class;
		check(marker.isInterface(), "it's an interface");
		check(Modifier.isPublic(marker.getModifiers()), "it's public");
		check(marker.getDeclaredMethods().length == 0, "it declares no methods");
		check(marker.getDeclaredFields().length == 0, "it declares no fields");
		check(marker.getInterfaces().length == 0, "it extends nothing");
		check(!marker.isSealed(), "anything can implement it");

		Object sku = new Sku("ABC-123456");
		check(sku instanceof TypedString, "a record implementing it is a TypedString");
		check(sku.toString().equals("ABC-123456"), "the example's toString gives back the value");
		check(new Sku("ABC-123456").equals(sku), "the example compares by value");
		try {
			new Sku("abc-123456");
			check(false, "the example refuses a value of the wrong shape");
		} catch (IllegalArgumentException expected) {
			check(String.valueOf(expected.getMessage()).equals("not a SKU: abc-123456"), "the example's refusal names the value");
		}

		System.out.println(checks - failures + "/" + checks + " checks passed");
		System.exit(failures == 0 ? 0 : 1);
	}

	private static void check(boolean passed, String what) {
		checks++;
		if (!passed) {
			failures++;
			System.out.println("FAIL: " + what);
		}
	}
}
