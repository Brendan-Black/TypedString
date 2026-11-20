# TypedString

[![JitPack](https://jitpack.io/v/Brendan-Black/TypedString.svg)](https://jitpack.io/#Brendan-Black/TypedString)

`black.TypedString`: a marker interface for a type that exists to hold and validate one shaped string, such as a SKU,
a MAC address or a host name. It declares nothing; "find implementations" on it is the catalogue of every shaped value
in a codebase. One interface, no dependencies, Java 17+.

```java
record Sku(String value) implements TypedString {
	private static final Pattern SHAPE = Pattern.compile("[A-Z]{3}-\\d{6}");

	public Sku {
		if (!SHAPE.matcher(value).matches()) {
			throw new IllegalArgumentException("not a SKU: " + value);
		}
	}

	@Override
	public String toString() {
		return value;
	}
}
```

See [*Typed strings: wrap them*](https://github.com/Brendan-Black/black/blob/master/conventions/values/typed-strings.md).

## Install

Via [JitPack](https://jitpack.io/#Brendan-Black/TypedString). Use `api` rather than `implementation` in a library
whose public types implement it.

```kotlin
repositories { maven("https://jitpack.io") }
dependencies { implementation("com.github.Brendan-Black:TypedString:1.0.0") }
```

[black-java](https://github.com/Brendan-Black/black-java)'s `lib` carries it too, as a dependency.

## Build

`./gradlew check` checks formatting, compiles with every warning as an error, runs the tests (`test/`, a plain
`main()`: the interface stays empty, and the example above works as written), and checks the jar holds only
`TypedString` and lists no dependencies.

## License

MIT
