package com.example.officetracker.ui.onboarding;

import com.example.officetracker.data.prefs.UserPreferences;
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
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  private final Provider<UserPreferences> userPreferencesProvider;

  public OnboardingViewModel_Factory(Provider<UserPreferences> userPreferencesProvider) {
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public OnboardingViewModel get() {
    return newInstance(userPreferencesProvider.get());
  }

  public static OnboardingViewModel_Factory create(
      Provider<UserPreferences> userPreferencesProvider) {
    return new OnboardingViewModel_Factory(userPreferencesProvider);
  }

  public static OnboardingViewModel newInstance(UserPreferences userPreferences) {
    return new OnboardingViewModel(userPreferences);
  }
}
