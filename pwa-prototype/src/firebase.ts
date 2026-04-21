import { initializeApp } from 'firebase/app'
import {
  GoogleAuthProvider,
  getAuth,
} from 'firebase/auth'
import {
  enableIndexedDbPersistence,
  getFirestore,
} from 'firebase/firestore'

const firebaseConfig = {
  apiKey: 'AIzaSyDVgNljprvdSnhj_P7xTNRsky_SQwXzwvA',
  authDomain: 'prodash-reminders.firebaseapp.com',
  projectId: 'prodash-reminders',
  storageBucket: 'prodash-reminders.firebasestorage.app',
  messagingSenderId: '948809119707',
  appId: '1:948809119707:web:spaplnadjur8eogs9sbavv4pqj9l259i',
}

const app = initializeApp(firebaseConfig)

export const auth = getAuth(app)
export const googleProvider = new GoogleAuthProvider()
export const db = getFirestore(app)

enableIndexedDbPersistence(db).catch(() => {
  // Ignore persistence errors; app remains functional online.
})
