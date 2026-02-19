package com.example.officetracker.ui.settings;

import com.example.officetracker.data.prefs.UserPreferences;
import com.example.officetracker.data.repository.AttendanceRepository;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<AttendanceRepository> repositoryProvider;

  public SettingsViewModel_Factory(Provider<UserPreferences> userPreferencesProvider,
      Provider<AttendanceRepository> repositoryProvider) {
    this.userPreferencesProvider = userPreferencesProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(userPreferencesProvider.get(), repositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<UserPreferences> userPreferencesProvider,
      Provider<AttendanceRepository> repositoryProvider) {
    return new SettingsViewModel_Factory(userPreferencesProvider, repositoryProvider);
  }

  public static SettingsViewModel newInstance(UserPreferences userPreferences,
      AttendanceRepository repository) {
    return new SettingsViewModel(userPreferences, repository);
  }
}
