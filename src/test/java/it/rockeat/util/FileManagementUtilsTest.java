package it.rockeat.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class FileManagementUtilsTest {

	@Test
	public void test() {
		String test[] = {"is this ever happened?","is this ever happened"};
		assertEquals(test[1], FileManagementUtils.escapeSpecialCharsFromFilename(test[0]));
	}

}
