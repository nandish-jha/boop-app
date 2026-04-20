// Motivational quotes — shuffled daily on the home screen
const QUOTES = [
  { q: "The secret of getting ahead is getting started.", a: "Mark Twain" },
  { q: "Discipline is the bridge between goals and accomplishment.", a: "Jim Rohn" },
  { q: "You do not rise to the level of your goals. You fall to the level of your systems.", a: "James Clear" },
  { q: "It always seems impossible until it's done.", a: "Nelson Mandela" },
  { q: "Small daily improvements are the key to staggering long-term results.", a: "Robin Sharma" },
  { q: "Action is the foundational key to all success.", a: "Pablo Picasso" },
  { q: "Well done is better than well said.", a: "Benjamin Franklin" },
  { q: "Do what you can, with what you have, where you are.", a: "Theodore Roosevelt" },
  { q: "The best way to predict the future is to create it.", a: "Peter Drucker" },
  { q: "Success is the sum of small efforts, repeated day in and day out.", a: "Robert Collier" },
  { q: "Motivation gets you going, but discipline keeps you growing.", a: "John C. Maxwell" },
  { q: "You miss 100% of the shots you don't take.", a: "Wayne Gretzky" },
  { q: "What gets measured gets managed.", a: "Peter Drucker" },
  { q: "Energy and persistence conquer all things.", a: "Benjamin Franklin" },
  { q: "Don't count the days, make the days count.", a: "Muhammad Ali" },
  { q: "Quality is not an act, it is a habit.", a: "Aristotle" },
  { q: "Whether you think you can, or think you can't — you're right.", a: "Henry Ford" },
  { q: "Fall seven times, stand up eight.", a: "Japanese proverb" },
  { q: "The journey of a thousand miles begins with one step.", a: "Lao Tzu" },
  { q: "Strive for progress, not perfection.", a: "Anonymous" },
  { q: "Your only limit is you.", a: "Anonymous" },
  { q: "Focus on being productive instead of busy.", a: "Tim Ferriss" },
  { q: "Simplicity is the ultimate sophistication.", a: "Leonardo da Vinci" },
  { q: "Done is better than perfect.", a: "Sheryl Sandberg" },
  { q: "The man who moves a mountain begins by carrying away small stones.", a: "Confucius" },
  { q: "If you want to lift yourself up, lift up someone else.", a: "Booker T. Washington" },
  { q: "What we do today echoes in our habits tomorrow.", a: "Anonymous" },
  { q: "Be so good they can't ignore you.", a: "Steve Martin" },
  { q: "Doing what you love is freedom. Loving what you do is happiness.", a: "Anonymous" },
  { q: "Success is not final, failure is not fatal: it is the courage to continue that counts.", a: "Winston Churchill" }
];

function getDailyQuote() {
  // Deterministic per-day pick — stable through the day, rotates daily.
  const d = new Date();
  const seed = d.getFullYear() * 10000 + (d.getMonth() + 1) * 100 + d.getDate();
  return QUOTES[seed % QUOTES.length];
}

function getGreeting() {
  const h = new Date().getHours();
  if (h < 5) return "Still up?";
  if (h < 12) return "Good morning";
  if (h < 17) return "Good afternoon";
  if (h < 21) return "Good evening";
  return "Good night";
}
