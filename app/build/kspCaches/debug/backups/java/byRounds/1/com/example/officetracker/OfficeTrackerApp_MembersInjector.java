package com.example.officetracker;

import androidx.hilt.work.HiltWorkerFactory;
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
public final class OfficeTrackerApp_MembersInjector implements MembersInjector<OfficeTrackerApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public OfficeTrackerApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<OfficeTrackerApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new OfficeTrackerApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(OfficeTrackerApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.example.officetracker.OfficeTrackerApp.workerFactory")
  public static void injectWorkerFactory(OfficeTrackerApp instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
