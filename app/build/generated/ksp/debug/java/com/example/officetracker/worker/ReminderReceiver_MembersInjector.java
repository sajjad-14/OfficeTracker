package com.example.officetracker.worker;

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
public final class ReminderReceiver_MembersInjector implements MembersInjector<ReminderReceiver> {
  private final Provider<NotificationHelper> notificationHelperProvider;

  public ReminderReceiver_MembersInjector(Provider<NotificationHelper> notificationHelperProvider) {
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public static MembersInjector<ReminderReceiver> create(
      Provider<NotificationHelper> notificationHelperProvider) {
    return new ReminderReceiver_MembersInjector(notificationHelperProvider);
  }

  @Override
  public void injectMembers(ReminderReceiver instance) {
    injectNotificationHelper(instance, notificationHelperProvider.get());
  }

  @InjectedFieldSignature("com.example.officetracker.worker.ReminderReceiver.notificationHelper")
  public static void injectNotificationHelper(ReminderReceiver instance,
      NotificationHelper notificationHelper) {
    instance.notificationHelper = notificationHelper;
  }
}
