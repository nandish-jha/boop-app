-keepattributes Signature,*Annotation*,EnclosingMethod,InnerClasses
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

-keep class com.prodash.reminders.BoopVoiceInteractionService { *; }
-keep class com.prodash.reminders.BoopVoiceInteractionSessionService { *; }
-keep class com.prodash.reminders.BoopRecognitionService { *; }
