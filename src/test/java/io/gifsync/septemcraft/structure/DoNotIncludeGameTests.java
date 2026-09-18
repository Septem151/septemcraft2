package io.gifsync.septemcraft.structure;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import java.util.regex.Pattern;

/** Keeps the gametest source set out of the analysis, the way ArchUnit keeps {@code src/test} out. */
public final class DoNotIncludeGameTests implements ImportOption
{
	private static final Pattern GAMETEST_OUTPUT = Pattern.compile(".*/build/classes/([^/]+/)?gametest/.*");

	/** Whether a location holds classes the rules apply to. */
	@Override
	public boolean includes(Location location)
	{
		return !location.matches(GAMETEST_OUTPUT);
	}
}
