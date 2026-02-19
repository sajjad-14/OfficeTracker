package com.example.officetracker.ui.analytics;

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
public final class AnalyticsViewModel_Factory implements Factory<AnalyticsViewModel> {
  private final Provider<AttendanceRepository> repositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  public AnalyticsViewModel_Factory(Provider<AttendanceRepository> repositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.repositoryProvider = repositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public AnalyticsViewModel get() {
    return newInstance(repositoryProvider.get(), userPreferencesProvider.get());
  }

  public static AnalyticsViewModel_Factory create(Provider<AttendanceRepository> repositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new AnalyticsViewModel_Factory(repositoryProvider, userPreferencesProvider);
  }

  public static AnalyticsViewModel newInstance(AttendanceRepository repository,
      UserPreferences userPreferences) {
    return new AnalyticsViewModel(repository, userPreferences);
  }
}
