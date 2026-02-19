package com.example.officetracker.di;

import android.content.Context;
import com.google.android.gms.location.GeofencingClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideGeofencingClientFactory implements Factory<GeofencingClient> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideGeofencingClientFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public GeofencingClient get() {
    return provideGeofencingClient(contextProvider.get());
  }

  public static AppModule_ProvideGeofencingClientFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideGeofencingClientFactory(contextProvider);
  }

  public static GeofencingClient provideGeofencingClient(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideGeofencingClient(context));
  }
}
