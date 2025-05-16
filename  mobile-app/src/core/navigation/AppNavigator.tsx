import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator, StackNavigationOptions } from '@react-navigation/stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs'; // Example, could be drawer too

import { useAuth } from '../auth/AuthContext'; // REQ-14-010 (used for conditional rendering and role access)

// --- Placeholder Screens ---
// These screens would be defined in their respective feature modules.
// For now, simple functional components as placeholders.
const PlaceholderScreen: React.FC<{ route: { params?: { screenName?: string } } }> = ({ route }) => {
  const { View, Text, StyleSheet } = require('react-native');
  return (
    <View style={styles.container}>
      <Text style={styles.text}>{route.params?.screenName || 'Placeholder Screen'}</Text>
    </View>
  );
};
const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#f0f0f0' },
  text: { fontSize: 18, color: '#333' },
});

const LoginScreen: React.FC = () => <PlaceholderScreen route={{ params: { screenName: "Login Screen" } }} />;
const FarmerListScreen: React.FC = () => <PlaceholderScreen route={{ params: { screenName: "Farmer List Screen" } }} />;
const LandListScreen: React.FC = () => <PlaceholderScreen route={{ params: { screenName: "Land List Screen" } }} />;
const CropCycleListScreen: React.FC = () => <PlaceholderScreen route={{ params: { screenName: "Crop Cycle List Screen" } }} />;
const SyncStatusScreen: React.FC = () => <PlaceholderScreen route={{ params: { screenName: "Sync Status Screen" } }} />;
const ConflictListScreen: React.FC = () => <PlaceholderScreen route={{ params: { screenName: "Conflict List Screen" } }} />;
const ConflictDetailScreen: React.FC = () => <PlaceholderScreen route={{ params: { screenName: "Conflict Detail Screen" } }} />;
// --- End Placeholder Screens ---

// Define Param types for navigators
export type AuthStackParamList = {
  Login: undefined;
};

export type MainAppTabParamList = {
  Farmers: undefined;
  Lands: undefined;
  CropCycles: undefined;
  Sync: undefined;
  // Conflicts tab might be role-dependent
};

export type ConflictStackParamList = {
  ConflictList: undefined;
  ConflictDetail: { conflictId: string }; // Example param
};

// Combine MainApp and ConflictStack into a single stack if conflicts are part of the main flow
export type AppStackParamList = {
  MainTabs: undefined; // Points to the TabNavigator
  ConflictResolution: { screen: keyof ConflictStackParamList, params?: ConflictStackParamList[keyof ConflictStackParamList] }; // Nested stack
};


const AuthStack = createStackNavigator<AuthStackParamList>();
const MainAppTabs = createBottomTabNavigator<MainAppTabParamList>();
const ConflictStack = createStackNavigator<ConflictStackParamList>();
const AppStack = createStackNavigator<AppStackParamList>();


const commonScreenOptions: StackNavigationOptions = {
  headerStyle: { backgroundColor: '#007AFF' },
  headerTintColor: '#fff',
  headerTitleStyle: { fontWeight: 'bold' },
};

function AuthScreens() {
  return (
    <AuthStack.Navigator screenOptions={commonScreenOptions}>
      <AuthStack.Screen name="Login" component={LoginScreen} options={{ title: 'Login' }} />
    </AuthStack.Navigator>
  );
}

// REQ-14-010: Conflict Resolution screens should be role-protected.
// Access to this stack or specific screens within it should be controlled.
function ConflictResolutionScreens() {
  // Role check can be done here or within individual screens using useAuth()
  // const { user } = useAuth();
  // if (user?.role !== 'Farm Plot Admin' && user?.role !== 'Admin') {
  //   return <PlaceholderScreen route={{ params: { screenName: "Access Denied to Conflicts" } }} />;
  // }
  return (
    <ConflictStack.Navigator screenOptions={commonScreenOptions}>
      <ConflictStack.Screen name="ConflictList" component={ConflictListScreen} options={{ title: 'Resolve Conflicts' }} />
      <ConflictStack.Screen name="ConflictDetail" component={ConflictDetailScreen} options={{ title: 'Conflict Details' }} />
    </ConflictStack.Navigator>
  );
}


function MainAppTabScreens() {
  const { user } = useAuth(); // REQ-14-010
  const canAccessConflicts = user?.role === 'Farm Plot Admin' || user?.role === 'Admin';

  return (
    <MainAppTabs.Navigator screenOptions={{ headerShown: false }}>
      <MainAppTabs.Screen name="Farmers" component={FarmerListScreen} /* options={{ tabBarIcon: ... }} */ />
      <MainAppTabs.Screen name="Lands" component={LandListScreen} />
      <MainAppTabs.Screen name="CropCycles" component={CropCycleListScreen} />
      <MainAppTabs.Screen name="Sync" component={SyncStatusScreen} />
      {/* Conflicts can be a tab or a separate navigation item from a settings/profile screen */}
      {/* If it's a tab and needs to be conditionally shown based on role: */}
      {/* {canAccessConflicts && (
        <MainAppTabs.Screen
          name="Conflicts"
          component={ConflictListScreen} // Direct or navigate to ConflictResolutionScreens stack
          options={{ title: "Conflicts" }}
        />
      )} */}
    </MainAppTabs.Navigator>
  );
}

function MainApplicationStack() {
  return (
    <AppStack.Navigator screenOptions={{ headerShown: false }}>
      <AppStack.Screen name="MainTabs" component={MainAppTabScreens} />
      {/* Conflict resolution can be a modal stack or pushed onto the main stack */}
      <AppStack.Screen 
        name="ConflictResolution" 
        component={ConflictResolutionScreens} 
        options={{ ...commonScreenOptions, headerShown: true, presentation: 'modal' }} 
      />
    </AppStack.Navigator>
  );
}


export default function AppNavigator() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    // You can render a loading indicator here
    const { View, ActivityIndicator } = require('react-native');
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator size="large" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      {isAuthenticated ? <MainApplicationStack /> : <AuthScreens />}
    </NavigationContainer>
  );
}