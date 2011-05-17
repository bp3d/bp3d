package jp.dbcls.bp3d;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Bp3dProperties {
	private static final String BUNDLE_NAME = "jp.dbcls.bp3d.bp3d"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Bp3dProperties() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
