package com.example.officetracker.ui.dashboard;

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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<AttendanceRepository> repositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public DashboardViewModel_Factory(Provider<AttendanceRepository> repositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.repositoryProvider = repositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(repositoryProvider.get(), userPreferencesProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<AttendanceRepository> repositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new DashboardViewModel_Factory(repositoryProvider, userPreferencesProvider);
  }

  public static DashboardViewModel newInstance(AttendanceRepository repository,
      UserPreferences userPreferences) {
    return new DashboardViewModel(repository, userPreferences);
  }
}
