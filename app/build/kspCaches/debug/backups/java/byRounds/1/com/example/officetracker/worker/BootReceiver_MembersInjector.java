package com.example.officetracker.worker;

import com.example.officetracker.data.prefs.UserPreferences;
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
public final class BootReceiver_MembersInjector implements MembersInjector<BootReceiver> {
  private final Provider<GeofenceManager> geofenceManagerProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<AlarmScheduler> alarmSchedulerProvider;

  public BootReceiver_MembersInjector(Provider<GeofenceManager> geofenceManagerProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    this.geofenceManagerProvider = geofenceManagerProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.alarmSchedulerProvider = alarmSchedulerProvider;
  }

  public static MembersInjector<BootReceiver> create(
      Provider<GeofenceManager> geofenceManagerProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<AlarmScheduler> alarmSchedulerProvider) {
    return new BootReceiver_MembersInjector(geofenceManagerProvider, userPreferencesProvider, alarmSchedulerProvider);
  }

  @Override
  public void injectMembers(BootReceiver instance) {
    injectGeofenceManager(instance, geofenceManagerProvider.get());
    injectUserPreferences(instance, userPreferencesProvider.get());
    injectAlarmScheduler(instance, alarmSchedulerProvider.get());
  }

  @InjectedFieldSignature("com.example.officetracker.worker.BootReceiver.geofenceManager")
  public static void injectGeofenceManager(BootReceiver instance, GeofenceManager geofenceManager) {
    instance.geofenceManager = geofenceManager;
  }

  @InjectedFieldSignature("com.example.officetracker.worker.BootReceiver.userPreferences")
  public static void injectUserPreferences(BootReceiver instance, UserPreferences userPreferences) {
    instance.userPreferences = userPreferences;
  }

  @InjectedFieldSignature("com.example.officetracker.worker.BootReceiver.alarmScheduler")
  public static void injectAlarmScheduler(BootReceiver instance, AlarmScheduler alarmScheduler) {
    instance.alarmScheduler = alarmScheduler;
  }
}
