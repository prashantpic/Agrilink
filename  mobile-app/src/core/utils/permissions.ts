import { PermissionsAndroid, Platform } from 'react-native';
import {
  check,
  request,
  PERMISSIONS,
  RESULTS,
  Permission,
  PermissionStatus,
} from 'react-native-permissions';

export type PermissionType = 'LOCATION' | 'CAMERA' | 'STORAGE_READ' | 'STORAGE_WRITE';

const getPlatformPermission = (permissionType: PermissionType): Permission | null => {
  if (Platform.OS === 'android') {
    switch (permissionType) {
      case 'LOCATION':
        return PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION;
      case 'CAMERA':
        return PERMISSIONS.ANDROID.CAMERA;
      case 'STORAGE_READ':
        return PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE; // Or READ_MEDIA_IMAGES / READ_MEDIA_VIDEO for API 33+
      case 'STORAGE_WRITE':
        return PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE; // Becoming restricted, mostly for pre-API 29
      default:
        return null;
    }
  } else if (Platform.OS === 'ios') {
    switch (permissionType) {
      case 'LOCATION':
        return PERMISSIONS.IOS.LOCATION_WHEN_IN_USE;
      case 'CAMERA':
        return PERMISSIONS.IOS.CAMERA;
      // iOS handles storage permissions differently (e.g., Photos through PERMISSIONS.IOS.PHOTO_LIBRARY)
      // General file storage is sandboxed.
      case 'STORAGE_READ': // Example, might map to PHOTO_LIBRARY or specific document access
      case 'STORAGE_WRITE':
        return PERMISSIONS.IOS.PHOTO_LIBRARY_ADD_ONLY; // Or PHOTO_LIBRARY for read/write
      default:
        return null;
    }
  }
  return null;
};

/**
 * Checks if a specific permission has already been granted.
 * @param permissionType The type of permission to check.
 * @returns Promise<boolean> True if granted, false otherwise.
 */
export const checkPermission = async (permissionType: PermissionType): Promise<boolean> => {
  const permission = getPlatformPermission(permissionType);
  if (!permission) {
    console.warn(`Permission type ${permissionType} not configured for ${Platform.OS}`);
    return false;
  }

  try {
    const result: PermissionStatus = await check(permission);
    switch (result) {
      case RESULTS.GRANTED:
      case RESULTS.LIMITED: // For iOS, limited access is often sufficient
        return true;
      case RESULTS.DENIED:
      case RESULTS.BLOCKED:
      case RESULTS.UNAVAILABLE:
      default:
        return false;
    }
  } catch (error) {
    console.error(`Error checking ${permissionType} permission:`, error);
    return false;
  }
};

/**
 * Requests a specific permission from the user.
 * @param permissionType The type of permission to request.
 * @param rationale Optional rationale for Android permissions.
 * @returns Promise<boolean> True if granted, false otherwise.
 */
export const requestPermission = async (
  permissionType: PermissionType,
  rationale?: { title: string; message: string; buttonPositive: string; buttonNegative?: string; buttonNeutral?: string; }
): Promise<boolean> => {
  const permission = getPlatformPermission(permissionType);
  if (!permission) {
    console.warn(`Permission type ${permissionType} not configured for ${Platform.OS}`);
    return false;
  }

  try {
    // For Android, react-native-permissions handles rationale internally for some permissions,
    // but for others, explicit rationale might still be useful via PermissionsAndroid API if needed.
    // The `request` function from `react-native-permissions` is generally preferred.
    if (Platform.OS === 'android' && rationale && permission === PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION) {
        // Example of using PermissionsAndroid directly if react-native-permissions doesn't fit a specific case
        // const granted = await PermissionsAndroid.request(
        //   PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        //   rationale,
        // );
        // return granted === PermissionsAndroid.RESULTS.GRANTED;
    }

    const result: PermissionStatus = await request(permission);
    switch (result) {
      case RESULTS.GRANTED:
      case RESULTS.LIMITED:
        return true;
      case RESULTS.DENIED:
        console.log(`${permissionType} permission denied by user.`);
        return false;
      case RESULTS.BLOCKED:
        console.log(`${permissionType} permission blocked by user (cannot request again).`);
        // Optionally, guide user to settings: Linking.openSettings();
        return false;
      case RESULTS.UNAVAILABLE:
         console.log(`${permissionType} permission unavailable on this device.`);
        return false;
      default:
        return false;
    }
  } catch (error) {
    console.error(`Error requesting ${permissionType} permission:`, error);
    return false;
  }
};

/**
 * Requests location permission specifically.
 * REQ-14-001 and REQ-1.3-001 imply GPS data capture, needing location permission.
 */
export const requestLocationPermission = async (): Promise<boolean> => {
  return requestPermission('LOCATION', {
    title: 'Location Permission',
    message: 'This app needs access to your location to capture GPS data for land records.',
    buttonPositive: 'Allow',
    buttonNegative: 'Deny',
  });
};

export const checkLocationPermission = async (): Promise<boolean> => {
  return checkPermission('LOCATION');
};

export const requestCameraPermission = async (): Promise<boolean> => {
    return requestPermission('CAMERA', {
        title: 'Camera Permission',
        message: 'This app needs access to your camera for taking photos.',
        buttonPositive: 'Allow',
        buttonNegative: 'Deny',
    });
};

export const checkCameraPermission = async (): Promise<boolean> => {
    return checkPermission('CAMERA');
};

// Add other specific permission request functions as needed (e.g., storage)