/* Seed data: workouts from Notion, supplements, skincare, budget categories, habits */
window.SEED = {
  habits: [
    { id: 'h_sleep', name: 'Sleep consistency', unit: 'hrs', target: 8, type: 'quant' },
    { id: 'h_water', name: 'Water intake', unit: 'cups', target: 8, type: 'quant' },
    { id: 'h_read',  name: 'Reading',         unit: 'min', target: 30, type: 'quant' },
    { id: 'h_medit', name: 'Meditation',      unit: 'min', target: 10, type: 'quant' },
    { id: 'h_pray',  name: 'Pray to god',     unit: 'times', target: 1, type: 'quant' },
    { id: 'h_food',  name: 'No outside food', unit: '',    target: 1, type: 'check' },
    { id: 'h_gym',   name: 'Gym',             unit: '',    target: 1, type: 'check' },
    { id: 'h_wake',  name: '4am wake up',     unit: '',    target: 1, type: 'check' },
    { id: 'h_grat',  name: 'Gratitude (night)', unit: '',  target: 1, type: 'check' }
  ],

  supplements: [
    { id: 's1', name: 'Vitamin D3', time: 'morning', dose: '1000 IU' },
    { id: 's2', name: 'Omega 3 Fish Oil', time: 'morning', dose: '1 cap (with food)' },
    { id: 's3', name: 'Zinc', time: 'morning', dose: '15 mg' },
    { id: 's4', name: 'Rhodiola Rosea', time: 'morning', dose: '300 mg' },
    { id: 's5', name: 'Probiotic', time: 'morning', dose: '1 cap' },
    { id: 's6', name: 'Whey Protein Isolate', time: 'workout', dose: '1 scoop (after)' },
    { id: 's7', name: 'Electrolytes', time: 'workout', dose: '1 serving' },
    { id: 's8', name: 'Creatine', time: 'workout', dose: '5 g' },
    { id: 's9', name: 'L-Citrulline', time: 'workout', dose: '6 g' },
    { id: 's10', name: 'Magnesium Glycinate', time: 'night', dose: '400 mg' },
    { id: 's11', name: 'Ashwagandha', time: 'night', dose: '600 mg' },
    { id: 's12', name: 'Glycine', time: 'night', dose: '3 g' },
    { id: 's13', name: 'L-Theanine', time: 'night', dose: '200 mg' },
    { id: 's14', name: 'Inositol', time: 'night', dose: '2 g' }
  ],

  skincare: [
    { id: 'sk1', name: 'Gel cleanser', time: 'morning' },
    { id: 'sk2', name: 'Moisturizer', time: 'morning' },
    { id: 'sk3', name: 'Sunscreen', time: 'morning' },
    { id: 'sk4', name: 'Facewash', time: 'night' },
    { id: 'sk5', name: 'Moisturizer', time: 'night' },
    { id: 'sk6', name: 'Under-eye serum / gel strips (optional)', time: 'night' }
  ],

  budgetCategories: [
    'Hanging out with friends', 'Shopping', 'Phone bill',
    'Gym subscription', 'Perplexity AI subscription',
    'Groceries', 'Food', 'Transport', 'Rent', 'Utilities',
    'Transfer', 'Investment', 'Other'
  ],

  accounts: [
    { id: 'a1', name: 'Scotiabank Chequing', type: 'chequing', bank: 'Scotiabank' },
    { id: 'a2', name: 'Scotiabank Savings', type: 'savings', bank: 'Scotiabank' },
    { id: 'a3', name: 'Scotiabank TFSA', type: 'tfsa', bank: 'Scotiabank' },
    { id: 'a4', name: 'Scotiabank Credit Card', type: 'credit', bank: 'Scotiabank' },
    { id: 'a5', name: 'TD Chequing', type: 'chequing', bank: 'TD' },
    { id: 'a6', name: 'TD Savings', type: 'savings', bank: 'TD' },
    { id: 'a7', name: 'TD TFSA', type: 'tfsa', bank: 'TD' },
    { id: 'a8', name: 'TD Credit Card', type: 'credit', bank: 'TD' },
    { id: 'a9', name: 'Affinity Chequing', type: 'chequing', bank: 'Affinity' },
    { id: 'a10', name: 'Affinity Savings', type: 'savings', bank: 'Affinity' },
    { id: 'a11', name: 'Affinity TFSA', type: 'tfsa', bank: 'Affinity' },
    { id: 'a12', name: 'Wealthsimple Chequing', type: 'chequing', bank: 'Wealthsimple' },
    { id: 'a13', name: 'Wealthsimple TFSA', type: 'tfsa', bank: 'Wealthsimple' },
    { id: 'a14', name: 'Loblaw EOSP', type: 'equity', bank: 'Loblaw' },
    { id: 'a15', name: 'University Loan', type: 'loan', bank: 'Gov' }
  ],

  workouts: [
    {
      id: 'w1', name: 'Workout 1 - Some Random Website',
      note: 'Goes chest, shoulders, legs, back & abs, biceps & triceps, then cardio.',
      days: [
        { day: 'Chest', exercises: [
          'Flat bench barbell press — 4x6',
          'Bench press declined — 4x6',
          'Bench press inclined — 4x6',
          'Dumbbell flyes — 3x10',
          'Push-ups — 4x20',
          'Cable crossovers — 3x15'
        ]},
        { day: 'Shoulder', exercises: [
          'Shoulder press — 4x12',
          'Military press — 4x10-12',
          'Deltoid flyes — 3x6',
          'Upright rows — 4x6',
          'Dumbbell front raises — 4x12',
          'Lateral raises — 4x12'
        ]},
        { day: 'Legs', exercises: [
          'Barbell squats — 4x8-10',
          'Leg press — 3x10',
          'Leg extensions — 3x10',
          'Calf raises — 3x20',
          'Hack squats — 4x10',
          'Barbell forward lunges — 3x10'
        ]},
        { day: 'Back & Abs', exercises: [
          'Chin-ups / Lat pulldowns — 4x10',
          'Wide-grip lat pulldowns — 4x12',
          'Close-grip lat pulldowns — 4x12',
          'Dumbbell rows — 4x8-10 each arm',
          'Hyperextensions — 4 to failure',
          '+ core workout'
        ]},
        { day: 'Biceps & Triceps', exercises: [
          'Dumbbell curls — 4x10-12',
          'Preacher curls — 4x12',
          'Triceps extension — 4x10-12 each arm',
          'Triceps rope pushdowns — 4x15',
          'Skull crushers ropes — 4x10'
        ]},
        { day: 'Cardio', exercises: ['Steady-state 60 min (rower or treadmill)'] }
      ]
    },
    {
      id: 'w2', name: 'Workout 2 - Hrithik Inspired',
      note: 'Cardio 20 min daily. Tips: rotate routine, stretch first, high reps low weight, sleep well.',
      days: [
        { day: 'Mon — Chest, Calves & Back', exercises: [
          'DB bench press — 5 sets (3 working 5-10, 2 warmup)',
          'Incline DB flyes — 2x5-20',
          'Underhand cable pulldown — 4 sets (3 working, 1 warmup) 5-10',
          'Bent over barbell row — 4 sets (3 working 6-10, 1 warmup)',
          'Back extensions — 3 sets (2 working 8-10)',
          'Seated calf raise — 3x20',
          'Standing calf raise — 3x16-20'
        ]},
        { day: 'Tue — Legs', exercises: [
          'Leg press — 8 sets (3 working x10, 5 warmup)',
          'Seated leg tucks — 5 sets (3 working x15)',
          'Extensions — 4 sets (2 working 16-20)',
          'Lying leg curls — 5 sets (3 working x15)',
          'Squats — 5 sets (3 working 15-30)'
        ]},
        { day: 'Thu — Shoulders, Calves & Abs', exercises: [
          'Marble military press — 5 sets (3 working x8)',
          'Side lateral raise — 4 sets (3 working 10-15)',
          'Upright barbell row — 3x7',
          'Reverse flyes — 7x12',
          'Weighted sit-ups — 3x20',
          'Seated calf raise — 3x20',
          'Standing calf raise — 3x20'
        ]},
        { day: 'Fri — Arms', exercises: [
          'Straight-arm DB pullover — 2x10',
          'Cable rope tricep extension (overhead) — 3x10-12',
          'Cable lying tricep extension — 3x10',
          'Standing DB tricep extension — 3x8-10',
          'Straight-arm pulldown — 7x15',
          'Concentration curls — 5 sets (3 working 10-15)',
          'Alternate DB bicep curl — 5 sets (3 working 12-15)',
          'Standard bicep cable curl — 3x15-20'
        ]}
      ]
    },
    {
      id: 'w7', name: 'Workout 7 - CrossFit 1',
      days: [
        { day: 'Mon', exercises: ['Squats 5x5', 'Deadlifts 5,5,3,2', 'Walking lunges 3x12', 'Sled pushes 4x45m', 'Air cycle cardio'] },
        { day: 'Tue', exercises: ['Pull-ups 5x20', 'Bent over rows 3x12', 'Ball slams 3x10', 'Rower sprints 30/30 x10'] },
        { day: 'Wed', exercises: ['Rest'] },
        { day: 'Thu', exercises: ['Flat bench press 10x10', 'DB incline press 3x10', 'DB flyes 3x15', 'Dips 3xFailure'] },
        { day: 'Fri', exercises: ['Thrusters 3x12', 'Single-arm shoulder press 5x5', 'Lateral raises 4x12', 'Hammer curls 3x10', 'Barbell curls 3x10 (close/reg/wide)'] },
        { day: 'Sat', exercises: ['Circuit: Pull-ups 5 + Push-ups 10 + Turkish get-ups 50-100'] },
        { day: 'Sun', exercises: ['Sprints + Rowing / similar cardio'] }
      ]
    },
    {
      id: 'w9', name: 'Workout 9 - ChatGPT',
      days: [
        { day: 'Day 1 — Chest & Triceps', exercises: ['Bench press 4x8-10', 'Incline DB press 3x10-12', 'Chest flyes 3x12-15', 'Tricep dips 4x10-12', 'Tricep rope pushdowns 3x12-15', 'Overhead DB extension 3x12-15'] },
        { day: 'Day 2 — Back & Biceps', exercises: ['Deadlifts 4x8-10', 'Lat pulldowns 3x10-12', 'Bent over rows 3x10-12', 'Barbell curl 4x10-12', 'Hammer curls 3x12-15', 'Preacher curls 3x12-15'] },
        { day: 'Day 3 — Legs', exercises: ['Squats 4x8-10', 'Leg press 3x10-12', 'Lunges 3x12-15 per leg', 'Leg curls 4x10-12', 'Calf raises 4x12-15'] },
        { day: 'Day 4 — Shoulders', exercises: ['Military press 4x8-10', 'Lateral raises 3x12-15', 'Front raises 3x12-15', 'Face pulls 3x12-15', 'Shrugs 4x10-12'] },
        { day: 'Day 5 — Cardio & Abs', exercises: ['30 min HIIT or steady cardio', 'Planks, Russian twists, leg raises 3x15-20'] },
        { day: 'Day 6 — Arms & Abs', exercises: ['Close-grip bench 4x8-10', 'Skull crushers 3x12-15', 'Concentration curls 4x10-12', 'Cable hammer curls 3x12-15', 'Russian twists 3x20/side', 'Bicycle crunches 3x20/side'] },
        { day: 'Day 7', exercises: ['Rest / active recovery'] }
      ]
    },
    {
      id: 'w12', name: 'Workout 12 - Roadhouse (Jake Gyllenhaal)',
      note: '1 set each, 8-12 reps — full-body circuit.',
      days: [
        { day: 'Full body', exercises: [
          'Dips 1x8-12', 'Crunches 1x25', 'Weighted pull-ups 1x8-12',
          'Barbell squats 1x8-12', 'Barbell deadlift 1x8-12', 'DB shoulder press 1x8-12',
          'Walking lunge 1x8-12', 'DB shrugs 1x8-12', 'Bench press 1x8-12'
        ]}
      ]
    },
    {
      id: 'w14', name: 'Workout 14 - John Abraham',
      days: [
        { day: 'Day 1 — Chest & Triceps', exercises: ['Incline bench press 3x15', 'Decline bench press 3x15', 'DB fly 2x15', 'Dips 2x15', 'Tricep pushdown 3x15', 'Tricep kickback 2x15'] },
        { day: 'Day 2 — Back & Abs', exercises: ['Bent over row 4x15', 'Single DB row 4x15', 'Lat pulldown 2x15', 'Shrugs 3x15', 'Deadlift 3x15', 'Crunches 3x15', 'Leg raises 3x15'] },
        { day: 'Day 3 — Cardio & Abs', exercises: ['Crunches 3x15', 'Leg raises 3x15', 'Jogging 30 min'] },
        { day: 'Day 4 — Legs', exercises: ['Squats 4x15', 'Lunges 2x15', 'Leg press 2x15', 'Leg curl 3x15', 'Standing calf raise 3x15', 'Sitting calf raise 3x15'] },
        { day: 'Day 5 — Shoulders & Biceps', exercises: ['Overhead press 3x15', 'Side lateral raises 4x15', 'Rear delt raise 2x15', 'Barbell curl 3x15', 'DB curl (alt) 2x15', 'Hammer curl 2x15'] },
        { day: 'Day 6 — Cardio & Abs', exercises: ['Crunches 3x15', 'Leg raises 3x15', 'Jogging 30 min'] },
        { day: 'Day 7', exercises: ['Rest'] }
      ]
    },
    {
      id: 'w16', name: 'Workout 16 - Hybrid 2026',
      days: [
        { day: 'Mon — Lift + Easy Run', exercises: ['Bench press 3x8-10', 'Bent over rows 3x8-10', 'Overhead press 3x10', 'Pull-ups/Lat pulldowns 3xFailure', 'Run 20-30 min Zone 2'] },
        { day: 'Tue — Intervals', exercises: ['10 min light jog', '8 x 400m sprints (or 1 min fast / 1 min slow)', '5 min walk cooldown'] },
        { day: 'Wed — Legs', exercises: ['Back squats 3x6-8', 'Romanian deadlifts 3x10-12', 'Leg press or lunges 3x12', 'Calf raises 4x15'] },
        { day: 'Thu — Easy Run', exercises: ['30-45 min steady easy pace (nose breathing)'] },
        { day: 'Fri — Upper Hypertrophy', exercises: ['Incline DB press 3x12', 'Seated cable rows 3x12', 'Lateral raises 4x15', 'Bicep curls + Tricep ext 3x12-15 (superset)'] },
        { day: 'Sat — Long Run', exercises: ['60-90+ min easy / time on feet'] },
        { day: 'Sun', exercises: ['Rest'] }
      ]
    },
    {
      id: 'w_her', name: "Her version",
      days: [
        { day: 'Day 1 — Upper 1 & Core', exercises: ['Pull-ups', 'Chin-ups', 'Back rows', 'Seated bicep/hammer curls', 'Abs: Russian twists, alt leg raises, wiper leg raises, side toe touches'] },
        { day: 'Day 2 — Cardio', exercises: ['Treadmill 2 on/off x 20 min', 'Rowing 8/3/8 min', 'Skipping 3x100'] },
        { day: 'Day 3 — Upper 2 & Core', exercises: ['Push-ups', 'Tricep dips', 'Cuban press', 'Shoulder 2', 'Abs set'] },
        { day: 'Day 4 — Cardio', exercises: ['Treadmill 2 on/off x 20 min', 'Air cycle 8/3/8', 'Burpees 3x10 (start 5)'] },
        { day: 'Day 5 — Lower & Core', exercises: ['Barbell squats', 'Romanian deadlifts', 'Walking lunges', 'Goblet squats', 'Abs set'] },
        { day: 'Day 6 — Cardio', exercises: ['Incline walk 20 min', 'DB swings 3x10', 'Wall balls 3x10'] }
      ]
    }
  ]
};
