package com.example.officetracker.data.repository;

import android.content.Context;
import com.example.officetracker.data.local.dao.AttendanceDao;
import com.example.officetracker.data.prefs.UserPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AttendanceRepository_Factory implements Factory<AttendanceRepository> {
  private final Provider<AttendanceDao> attendanceDaoProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<Context> contextProvider;

  public AttendanceRepository_Factory(Provider<AttendanceDao> attendanceDaoProvider,
      Provider<UserPreferences> userPreferencesProvider, Provider<Context> contextProvider) {
    this.attendanceDaoProvider = attendanceDaoProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public AttendanceRepository get() {
    return newInstance(attendanceDaoProvider.get(), userPreferencesProvider.get(), contextProvider.get());
  }

  public static AttendanceRepository_Factory create(Provider<AttendanceDao> attendanceDaoProvider,
      Provider<UserPreferences> userPreferencesProvider, Provider<Context> contextProvider) {
    return new AttendanceRepository_Factory(attendanceDaoProvider, userPreferencesProvider, contextProvider);
  }

  public static AttendanceRepository newInstance(AttendanceDao attendanceDao,
      UserPreferences userPreferences, Context context) {
    return new AttendanceRepository(attendanceDao, userPreferences, context);
  }
}
