package com.example.officetracker.di;

import com.example.officetracker.data.local.AppDatabase;
import com.example.officetracker.data.local.dao.AttendanceDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideAttendanceDaoFactory implements Factory<AttendanceDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideAttendanceDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public AttendanceDao get() {
    return provideAttendanceDao(databaseProvider.get());
  }

  public static AppModule_ProvideAttendanceDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideAttendanceDaoFactory(databaseProvider);
  }

  public static AttendanceDao provideAttendanceDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAttendanceDao(database));
  }
}
