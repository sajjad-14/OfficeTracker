package com.example.officetracker;

import com.example.officetracker.data.prefs.UserPreferences;
import com.example.officetracker.worker.AlarmScheduler;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  public MainActivity_MembersInjector(Provider<UserPreferences> userPreferencesProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    this.userPreferencesProvider = userPreferencesProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<UserPreferences> userPreferencesProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    return new MainActivity_MembersInjector(userPreferencesProvider, alarmSchedulerProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectUserPreferences(instance, userPreferencesProvider.get());
    injectAlarmScheduler(instance, alarmSchedulerProvider.get());
  }

  @InjectedFieldSignature("com.example.officetracker.MainActivity.userPreferences")
  public static void injectUserPreferences(MainActivity instance, UserPreferences userPreferences) {
    instance.userPreferences = userPreferences;
  }

  @InjectedFieldSignature("com.example.officetracker.MainActivity.alarmScheduler")
  public static void injectAlarmScheduler(MainActivity instance, AlarmScheduler alarmScheduler) {
    instance.alarmScheduler = alarmScheduler;
  }
}
