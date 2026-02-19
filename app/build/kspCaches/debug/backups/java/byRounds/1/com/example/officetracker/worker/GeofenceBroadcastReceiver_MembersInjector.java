package com.example.officetracker.worker;

import com.example.officetracker.data.repository.AttendanceRepository;
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
public final class GeofenceBroadcastReceiver_MembersInjector implements MembersInjector<GeofenceBroadcastReceiver> {
  private final Provider<AttendanceRepository> repositoryProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  public GeofenceBroadcastReceiver_MembersInjector(
      Provider<AttendanceRepository> repositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    this.repositoryProvider = repositoryProvider;
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public static MembersInjector<GeofenceBroadcastReceiver> create(
      Provider<AttendanceRepository> repositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    return new GeofenceBroadcastReceiver_MembersInjector(repositoryProvider, notificationHelperProvider);
  }

  @Override
  public void injectMembers(GeofenceBroadcastReceiver instance) {
    injectRepository(instance, repositoryProvider.get());
    injectNotificationHelper(instance, notificationHelperProvider.get());
  }

  @InjectedFieldSignature("com.example.officetracker.worker.GeofenceBroadcastReceiver.repository")
  public static void injectRepository(GeofenceBroadcastReceiver instance,
      AttendanceRepository repository) {
    instance.repository = repository;
  }

  @InjectedFieldSignature("com.example.officetracker.worker.GeofenceBroadcastReceiver.notificationHelper")
  public static void injectNotificationHelper(GeofenceBroadcastReceiver instance,
      NotificationHelper notificationHelper) {
    instance.notificationHelper = notificationHelper;
  }
}
