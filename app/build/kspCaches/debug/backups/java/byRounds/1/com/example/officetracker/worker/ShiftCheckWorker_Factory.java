package com.example.officetracker.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.example.officetracker.data.repository.AttendanceRepository;
import dagger.internal.DaggerGenerated;
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
public final class ShiftCheckWorker_Factory {
  private final Provider<AttendanceRepository> repositoryProvider;

  private final Provider<NotificationHelper> notificationHelperProvider;

  public ShiftCheckWorker_Factory(Provider<AttendanceRepository> repositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    this.repositoryProvider = repositoryProvider;
    this.notificationHelperProvider = notificationHelperProvider;
  }

  public ShiftCheckWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, repositoryProvider.get(), notificationHelperProvider.get());
  }

  public static ShiftCheckWorker_Factory create(Provider<AttendanceRepository> repositoryProvider,
      Provider<NotificationHelper> notificationHelperProvider) {
    return new ShiftCheckWorker_Factory(repositoryProvider, notificationHelperProvider);
  }

  public static ShiftCheckWorker newInstance(Context context, WorkerParameters params,
      AttendanceRepository repository, NotificationHelper notificationHelper) {
    return new ShiftCheckWorker(context, params, repository, notificationHelper);
  }
}
