package com.example.officetracker.ui.onboarding;

import com.example.officetracker.data.prefs.UserPreferences;
import com.example.officetracker.worker.GeofenceManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class SetupViewModel_Factory implements Factory<SetupViewModel> {
  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<GeofenceManager> geofenceManagerProvider;

  public SetupViewModel_Factory(Provider<UserPreferences> userPreferencesProvider,
      Provider<GeofenceManager> geofenceManagerProvider) {
    this.userPreferencesProvider = userPreferencesProvider;
    this.geofenceManagerProvider = geofenceManagerProvider;
  }

  @Override
  public SetupViewModel get() {
    return newInstance(userPreferencesProvider.get(), geofenceManagerProvider.get());
  }

  public static SetupViewModel_Factory create(Provider<UserPreferences> userPreferencesProvider,
      Provider<GeofenceManager> geofenceManagerProvider) {
    return new SetupViewModel_Factory(userPreferencesProvider, geofenceManagerProvider);
  }

  public static SetupViewModel newInstance(UserPreferences userPreferences,
      GeofenceManager geofenceManager) {
    return new SetupViewModel(userPreferences, geofenceManager);
  }
}
