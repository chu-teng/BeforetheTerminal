# Before the Terminal Guide

**English** | [简体中文](GUIDE.zh-CN.md)

## Rules

### Introduction

Press **K**, or click the button at the top left of your inventory during a round, to read this guide. Survive aboard an express train bound for the terminal station and uncover the truth before it arrives.

The passenger faction must kill all murderers and outsiders, or survive until the train reaches the terminal. The murderer faction must eliminate every other faction except solo neutrals. Each outsider belongs to a separate outsider faction with the same objective: eliminate every other faction except solo neutrals. Solo neutrals have their own victory conditions, independent of whether anyone else survives. Their survival or death does not affect other factions' victory conditions.

Players know their own role, traits and the current encounter. At the start of a round, each player wakes in their room with a room key and a letter. Looking at another player reveals their name. Communication is limited to proximity voice chat.

### Roles

**Categories.** Passengers are Enforcers or Civilians; murderers are Principals or Accomplices; neutrals are Mad, Solo or Outsider. Their ID prefixes are E, C, P, A, M, S and O, respectively; suffixes A, B and C identify the script. The number is the role’s position within its script, not the number of starting seats. Mad Neutrals occupy neutral seats; each role specifies its actual faction.

**Seats.** For N players, Enforcers = N // 3, murderers = N // 6 (one Principal, the rest Accomplices), Mad Neutrals = 1 + N // 8, Solo Neutrals = N // 8, and one Outsider Neutral at 12 or more players. All remaining seats are Civilian Passengers. A standard 18-player round has 6 Enforcers, 3 Civilians, 1 Principal, 2 Accomplices, 3 Mad Neutrals, 2 Solo Neutrals and 1 Outsider. Roles generally do not repeat; roles sharing a color may appear together.

**Factions.** Roles and factions may change during a round. Generally, passengers and solo neutrals have limited stamina: when it runs too low, they cannot sprint and must stand still to recover. Murderers and outsiders have unlimited stamina. Only murderers and outsiders can see the arrival countdown. It starts at eight minutes and gains one minute whenever a passenger dies. Murderer instinct reveals all players through walls: murderers glow red, and everyone else glows green. Only passengers drop their guns and master keys on death, and only passengers can pick them up. In particular, if an Enforcer joins any other faction after having dropped their gun or bow, they receive it again; it is removed when they return to the passenger faction. Some players are told a different role from their actual technical role; this is a perception override. Some roles use another role's abilities, while others temporarily become a different role. The role initially assigned to a player is their initial role.

### Round Flow

**Executions.** When a passenger kills someone with a gun, bow or knife, it counts as an execution. Executing a passenger is a wrongful kill. A wrongful kill causes the executor to die by suicide only when performed with a bow. Every wrongful kill grants all murderers 100 madness. Performing an execution costs 40 sanity and makes the executor drop their gun. A player immune to execution survives with a shield-breaking sound, but the wrongful-kill check still applies.

**Death.** Death leaves a body. Some roles can remove bodies. Causes of death include accident, mental breakdown, gunshot, arrow, suicide, stabbing, magic, mishap, blunt weapon, axe, poison, bomb, grenade, flying sword, dagger, helium, curtain call, bloom, martyrdom, burial sacrifice and dying for love.

**Epilogues.** When the combined number of murderers and outsiders is at least the number of passengers, the countdown is reduced to two minutes. An epilogue begins whenever the countdown falls below two minutes, with background music playing during it. Passengers can trigger the survival epilogue, while outsiders can trigger their own epilogues. If the featured faction is eliminated and another outsider faction remains, the epilogue switches to that faction's epilogue; otherwise, it becomes the survival epilogue. The featured faction wins when its epilogue ends.

**Endings.** Passenger victory has two different quotes: passengers win either by killing all murderers and Outsider Neutrals before arrival, or by reaching the terminal before killing them all. Murderer victory also has two quotes: murderers win by killing all passengers and Outsider Neutrals while the Principal Murderer is still alive, or Accomplice Murderers finish the task after the Principal has died. Black Death and Heretic have special passenger and murderer victory endings with their own quotes. If multiple victory conditions are met at once, priority is Conductor > Black Death > Relationship Traits > Solo Neutrals > Outsiders > Murderers > Passengers.

### Abilities

**Sanity.** Sanity is the passenger resource. It starts at 100 and decreases naturally. Below 55, players see incorrect items and become drunk; below 20, they also see incorrect skins. Reaching zero causes immediate death. Fulfilling a periodically generated need restores 20 sanity. Each new need is selected randomly from eating, drinking, sleeping, fresh air, company, solitude, bathing and music. Up to four needs can accumulate. After completing the sixth need, a player receives a task: return to their room, operate the control panel, or inspect the lookout seat. Completing it awards a random Door Bolt, Remote or Walkie-Talkie. This Walkie-Talkie uses the passenger channel; Undercover does not receive another Walkie-Talkie.

**Madness.** Madness is the murderer resource, used to buy items from the portable murderer shop. Only murderers have madness. They gain 5 every 10 seconds, stopping natural regeneration when madness exceeds 200. A kill grants 100 madness.

**Abilities.** Names in angle brackets, such as &lt;skills&gt;, denote active abilities. Nearby skills are used with the ability key and either affect someone beside you or require no target. Remote skills target any player through the inventory menu. Unless stated otherwise, all skills have an initial cooldown; abilities without a regular cooldown have a 30-second initial cooldown. Talents are passive abilities. The murderer shop also counts as an ability. Drunkenness is hidden from the affected player. While drunk, abilities, talents and shops fail: non-information abilities have no effect, and information abilities provide false information. Drunk murderers are refused when trying to spend madness.

### Setting

The train consists of:

1. Locomotive
2. Baggage car
3. Second-class sleeper: four two-person rooms
4. Dining car
5. Second-class sleeper
6. Bar car
7. First-class sleeper: two two-person rooms
8. Dining car
9. First-class sleeper
10. Bar car
11. Crew sleeper: two two-person rooms
12. Baggage car
13. Guard's van

Baggage cars are open to the air and can fulfill the fresh-air need. Rooms contain beds and plates holding [Notes]. First-class rooms also have plates with [Firecrackers] and [Guns]. More players can occupy a room than its nominal capacity.

**Locomotive control panel:** Open its interface to see the current countdown and number of living players. Anything you say while using it is broadcast throughout the train.

**Guard's van lookout seat:** While seated, you see everyone on the train glowing green through walls. Immunity to instinct vision still applies.

**Chandelier mechanism:** Activates for five seconds and makes noise. If a player passes underneath, it becomes a falling block that kills any player it hits. It resets after landing.

**Wire mechanism:** Activates for five seconds and makes noise. It electrocutes players within a nearby area, accompanied by visual effects.

### Modes

**Draft Mode:** The default mode, for 6–22 players, balanced around 18. In random order, each player picks one of three roles or chooses Random. A specific pick is public; a Random pick stays hidden.

**Script Mode:** For 6–22 players. Only the passenger and murderer factions exist: Solo Neutrals join the passengers and Outsider Neutrals join the murderers. When a Solo Neutral fulfills their objective, they become permanently drunk and receive a gun. Each round uses one of three scripts: Trouble Brewing (A), Bad Moon Rising (B), or Sects & Violets (C). Every role category is drawn from that script. Forced roles may come from outside it and override normal seat quotas.

**Debate Mode:** 6–14 players, balanced around 12. Enforcers' guns and bows become single-use. Veteran and Priest have single-use knives; Witch has no knife. Murderers cannot buy weapons. Reporting a body starts a one-minute meeting and makes bodies from the current interval glow. Players then have 30 seconds to vote with notes. A player with the most votes and at least half the votes is executed. Tied leaders with at least half the votes become drunk until the next meeting instead. During voting, looking at anyone displays a single “Voters: …” line listing who voted for them, replacing other gaze information. This also applies in Reminiscence mode.

**Reminiscence Mode:** Based on Debate Mode. Opening and ability text reveal no role-identifying information. Use a note to guess your own role, receiving “Unrelated,” “Close” or “Perfect.” Cooldown: 30 seconds.

### Encounters

**Dream:** When three players remain, they use notes to decide whether to restart the round. If they agree, it immediately restarts with the same roles, but a two-minute countdown.

**Storm:** Everyone is told a passenger role that might be present. That role can die only by execution. If it is present, murderers also receive a note identifying the player.

**Fulfilment:** The game continues after a Solo Neutral fulfills their victory condition. They appear as a winner in the ending. Counteracting role: Monk.

**Hypocrisy:** Non-passengers can pick up guns and bows. Using one counts as an execution and makes them drop it. Non-passengers never trigger wrongful-kill checks, whether performing executions or other killings.

**Rebirth:** The first two passengers to die are resurrected as Solo Neutral roles not currently in play.

**Affinity:** Duplicate roles may appear.

**Supreme Good:** Everyone receives a note listing the round's role lineup. Counteracting roles: Heretic, Black Death.

**Judgment:** Anyone who commits a wrongful kill dies by suicide. Counteracting role: Virgin.

## Items

### Free Items

**[Food and Drinks]** Fulfill needs.

**[Note]** Write on it and place it on a wall, floor or ceiling.

**[Firecracker]** Produces gunshot sounds and is invisible to everyone except the player who placed it.

**[Fake Gun]** Looks like a real gun and can produce gunshot sounds.

**[Room Key]** Opens the door to a room.

**[Letter]** Right-click to open the Grimoire, which lists every player's portrait, number and name. You can annotate their faction, role and status, such as dead or missing. It also shows the number of players in each role category for the round. While holding the letter, players in view glow according to your annotations.

### Passenger Items

**[Gun]** Cooldown: 10 seconds after a miss, one minute after a hit.

**[Bow]** Must be drawn. No cooldown after a miss; one minute after a hit. A wrongful kill causes suicide.

**[Master Key]** Opens any door. Held by one random Enforcer and certain neutrals.

**[Door Bolt]** Jams a door for one minute. Cooldown: two minutes.

**[Remote]** Activates the nearest mechanism within 20 blocks. Cooldown: one minute.

### Murderer Items

**[Knife]** Requires charging. Murderer cooldown: one minute.

**[Walkie-Talkie]** Initially held by all murderers and Undercover. You must hold it to transmit, but not to listen.

**[Crowbar]** Breaks doors. Held by the Principal Murderer and certain outsiders.

The basic murderer shop sells: Grenade 250, Psychosis Mask 350, Derringer 400, Poison 25, Scorpion 25, Remote 100, Door Bolt 25, Master Key 50, Body Bag 50 and Circuit Breaker 400.

**[Poison]** Poisons a plate. A poisoned player hears a heartbeat warning and dies after one minute.

**[Scorpion]** Poisons a bed. Adjacent beds forming a double bed need only one application. A poisoned bed poisons anyone who sleeps in it, every time.

**[Psychosis Mask]** Enters psychosis mode for 30 seconds: your appearance changes, you gain one shield, and you use a bat with a charged attack to kill.

**[Derringer]** Holds one bullet. If you miss, killing someone by any means replenishes it.

**[Body Bag]** Removes a body.

**[Circuit Breaker]** Cuts power throughout the train, from anywhere.

Exclusive items are selected randomly each round from: Gun 400, Crossbow 400 and Poison Gas Bomb 200.

**[Crossbow]** Pierces targets and fires multiple projectiles. No cooldown after a miss; consumed after a hit.

**[Poison Gas Bomb]** Releases poisonous gas that fills a room.

## Passengers

### Enforcer Passengers

E1A【Vigilante】Starts with a [Gun]. Does not drop it after an execution. Unlimited sanity.

E2A【Railway Police】Starts with a [Gun]. Does not drop it after an execution. Unlimited stamina.

E3A【Witch】Starts with a [Knife] usable only once. &lt;Curse&gt; any player so that they die with you when you are killed. Changing the target has a one-minute cooldown.

E4A【Veteran】Starts with a [Knife].

E5A【Architect】&lt;Open Passage&gt; at your current location. After 30 seconds, you can teleport back there at any time. Cooldown: one minute.

E6A【Locksmith】&lt;Repair&gt; a jammed or forced-open door, or jam a door for one minute. Cooldown: 30 seconds. You sense whenever a door is jammed or forced open.

E7A【Postman】Other players can right-click you with an item in hand to give it to you. Right-click another player while holding an item to give it to them.

E8A【Collector】Instinct reveals dropped items and placed notes. &lt;Summon&gt; the nearest dropped item, making it fly into your hand. Cooldown: 30 seconds.

E1B【Hunter】Starts with a [Gun]. Once per game, &lt;Snipe&gt; any player by identifying them as the Principal Murderer. If correct, they die.

E2B【Judge】Starts with a [Gun]. &lt;Prosecute&gt; by correctly selecting every murderer in play, counting only roles in the murderer category. If correct, all selected murderers die. Cooldown: one minute.

E3B【Gunslinger】Starts with a [Gun]. Your executions always kill their targets.

E4B【Night Watchman】Starts with a [Gun]. Permanent night vision. You can see any player who enters psychosis mode through walls. On death, enters a five-second dying state.

E5B【Undercover】An ordinary passenger, except you have a [Walkie-Talkie] and appear red to murderer instinct.

E6B【Puppeteer】Select two players. If the first uses a targeted ability, all its targets are redirected to the second.

E7B【Craftsman】&lt;Replicate&gt; an item held by another player. After 30 seconds, you can give it to someone else. Cooldown: one minute.

E8B【Smuggler】&lt;Smuggle&gt; a nearby player into a possession state like Kidnapper. There is no countdown or digestion, and release does not reduce sanity based on time spent inside. Their voice originates from your position, without a separate channel. You may release them after 30 seconds. Cooldown: 30 seconds. Death releases them.

E1C【Caballero】Starts with a [Bow].

E2C【Ranger】Starts with a [Bow]. When using it, you die by suicide if the kill is not wrongful, and survive if it is wrongful.

E3C【Priest】Starts with a [Knife] usable three times. After you commit a wrongful kill, everyone is invulnerable for 30 seconds. Falls, mental breakdown and other self-inflicted deaths still apply.

E4C【Aviateur】When killed, leaves a body, revives in their room, receives a [Gun] and becomes invisible to murderer instinct.

E5C【Ex Undercover】Has the abilities of a random Accomplice Murderer not in play, while belonging to the passenger faction.

E6C【Snake Charmer】&lt;Charm&gt; any player. If they are the Principal Murderer, swap roles and factions with them. The new Snake Charmer becomes permanently drunk. Cooldown: one minute.

E7C【Cannibal】&lt;Devour&gt; a body. If it is a Civilian Passenger, temporarily become that role for one minute. Cooldown: one minute.

E8C【Philosopher】Once per game, &lt;Speculate&gt; any passenger role in the script and become that role. If it is present, its player is drunk until you die.

### Civilian Passengers

C1A【Doctor】Can see a body's role, cause of death and time of death.

C2A【Noble】You have three main-family members: two passengers and one non-passenger. With at least 12 players, you also have four branch-family members: a Civilian Passenger, an Accomplice Murderer, a Solo Neutral and an Outsider Neutral. The groups never overlap; looking at someone identifies membership. While drunk, each group is randomly drawn from outside its original list, still without overlap.

C3A【Pharmacist】Recognizes poisoned plates and beds. Poisoned players in view glow for you. &lt;Medicate&gt; a player to cure their poison, or fully restore their sanity if healthy. Cooldown: one minute.

C4A【Bartender】Choose a role to &lt;Pour&gt;. If present, all players of that role become drunk for two minutes. Cooldown: two minutes.

C5A【Journalist】&lt;Track&gt; any player to see them through walls until you change targets. Cooldown: one minute.

C6A【Engineer】Place an invisible &lt;Camera&gt; that continuously reports players passing by. Retrieve it before placing it elsewhere.

C7A【Empath】Each non-passenger within 20 blocks adds one times the normal sanity drain: one means double, two means triple. With four nearby, you immediately die from mental breakdown.

C8A【Detective】&lt;Examine&gt; a nearby player to learn whether they have killed anyone. Cooldown: one minute.

C1B【Mortician】Instinct reveals bodies. You can tell whether a body belonged to a passenger.

C2B【Folklorist】Whenever a player in view uses a remote ability, they glow for you.

C3B【Medium】&lt;Retain&gt; a player. After death, they continue to possess you. You can choose another only after the previous target becomes a spirit. When you die, the possessing spirit returns to life through your body and becomes permanently drunk.

C4B【Monk】&lt;Convert&gt; a nearby player. If they are a Solo Neutral, they die immediately upon fulfilling their victory condition and share victory at the ending. Cooldown: one minute. In Script Mode, instead grant them one shield.

C5B【Virgin】Immune to execution. &lt;Honor&gt; a nearby player, making them always die by suicide if they commit a wrongful kill. Cooldown: one minute.

C6B【Star】On death, glows for the passenger faction for 30 seconds, but not for other factions. &lt;Mark&gt; any player so that they glow with you when you die. Changing the target has a 30-second cooldown.

C7B【Professor】Use &lt;Vial&gt; to infuse a plate with a potion. Whoever consumes it gains one shield. Cooldown: two minutes.

C8B【Bodyguard】&lt;Escort&gt; a nearby player for 30 seconds: they can die only if you die. Cooldown: two minutes.

C1C【Investigator】&lt;Investigate&gt; a nearby player to learn one passenger role and one murderer role. The target is one of these; if they are a solo neutral or outsider, they are neither. Cooldown: one minute.

C2C【Prophet】&lt;Prophesy&gt; by selecting three players to learn whether any is a Principal Murderer. Selecting the same player twice instead compares whether the two distinct players are both passengers or both non-passengers. Cooldown: one minute.

C3C【Muyuubyou】&lt;Dream&gt; to leave your body motionless while your spirit observes the world. No cooldown.

C4C【Maid】Players in your view with at least three needs glow.

C5C【Conductor】Can see the arrival countdown. If you survive until only three players remain, your current faction wins.

C6C【Attendant】Can see the arrival countdown. While you live, it runs twice as fast.

C7C【Shoujo】Immune to instinct vision. Can &lt;Play Dead&gt;.

C8C【Poppy Grower】While you live, murderer instinct shows everyone as green.

## Murderers

### Principal Murderers

P1A【Assassin】Starts with a [Knife]. &lt;Identify&gt; another player's role. A correct guess kills them; an incorrect guess exposes your own role. Cooldown: one minute.

P2A【Agent】Starts with a [Knife]. Looking at a player reveals their role. Heretic appears as a random Civilian Passenger.

P3A【Actor】Starts with a [Knife]. Can &lt;Play Dead&gt;. Select a living player and either a living or dead player to &lt;Disguise&gt; the first as the second. No cooldown.

P4A【Stowaway】Starts with a [Knife]. &lt;Hide&gt; to become invisible for 30 seconds. Cooldown: two minutes.

P1B【Magician】Starts with a [Knife]. &lt;Swap&gt; any two players' positions. Cooldown: one minute.

P2B【Rigger】Starts with a [Knife]. &lt;Bind&gt; any two players for 30 seconds. Cooldown: one minute.

P3B【Imp】Starts with a [Knife]. &lt;Imprint&gt; a nearby player. If they are a neutral, they become Imp when you die. Changing the target has a one-minute cooldown.

P4B【Leech】Starts with a [Knife]. Once per game, &lt;Parasitize&gt; a nearby player. You can die only if they die.

P1C【Sculptor】&lt;Rebuild&gt; any passenger or murderer into a passenger or murderer role not currently in play, without changing their faction. Cooldown: two minutes.

P2C【Vortex】While you live, all Civilian Passengers are drunk. If two consecutive minutes pass without an execution, the murderers win.

P3C【Riot】While you live, anyone executed enters a five-second dying state and receives a gun that cannot be dropped.

P4C【Leviathan】While you live, the murderers win as soon as three wrongful kills have occurred.

### Accomplice Murderers

A1A【Alchemist】Starts with a [Poison Needle]. Inject poison into a nearby player, plate or bed. Cooldown: 30 seconds.

A2A【Cleaner】Starts with a [Dagger]. Silent, no charge time, one-minute cooldown. &lt;Cleanse&gt; dropped items. Cooldown: two minutes.

A3A【Terrorist】Starts with a [Bomb]. Cooldown: 30 seconds. It becomes visible five seconds after activation, can be passed on after holding it for three seconds, and explodes after a 15-second countdown. Either pass it to someone else or be caught in the blast yourself.

A4A【Warden】Starts with a [Cage]. Select three players with it; after a short delay, they enter the cage and become deaf and mute. They have 15 seconds to choose life or death. If all choose life, all die.

A5A【Harpy】Starts with a [Knife]. &lt;Shroud&gt; two different living players in order. Everyone is told the first target. If that target has not died within one minute, the second dies. Cooldown: two minutes.

A1B【Colonel】Starts with a [Gun]. Toggle &lt;Feign Virtue&gt; at any time: executions then follow passenger rules, including dropping and picking up guns, but never count as wrongful kills.

A2B【Lawyer】Starts with a [Gun]. &lt;Cover&gt; a nearby player, granting immunity to one execution. Cooldown: two minutes.

A3B【Succubus】Starts with a [Knife]. &lt;Bewitch&gt; any player, making them drunk for one minute.

A4B【Godfather】Starts with a [Knife]. &lt;Revenge&gt; any player. Whenever a neutral dies, your chosen target dies.

A5B【Mastermind】Starts with a [Knife]. When a Principal Murderer dies, the countdown is reduced to two minutes. A wrongful execution during that window makes the murderers win.

A1C【Comedian】Starts with a [Balloon]. Use it to raise a nearby player's voice pitch after a delay. Cooldown: 30 seconds. A second pitch change makes them explode. &lt;Inflate&gt; a nearby player so that attempting an execution makes them explode. Cooldown: one minute.

A2C【Psychopath】Starts with a [Fire Axe]. Drawing it grants one shield until you kill someone, after which the axe enters a one-minute cooldown.

A3C【Swordsman】Starts with a [Sword]. Throw a flying sword that pierces a line of players. Cooldown: two minutes.

A4C【Hikikomori】Starts with an [Iron Pipe]. Draw it to kill without charging for five seconds, followed by a two-minute cooldown.

A5C【Librarian】&lt;Isolate&gt; a nearby player after a delay, making them deaf and mute for 30 seconds. Cooldown: 30 seconds. During isolation, the louder someone speaks to the isolated player, the faster the speaker loses sanity, up to eight times the normal rate.

## Neutrals

### Mad Neutrals

M1A【Traitor】Belongs to the murderer faction. Starts with a [Knife]. Has no Walkie-Talkie and appears green to murderer instinct; otherwise behaves like other murderers.

M2A【Gambler】&lt;Wager&gt; on absent roles until you have correctly identified a number equal to one third of all absent roles, rounded down. An incorrect guess has a one-minute cooldown. You then become the last role you guessed correctly.

M3A【Damsel】All Accomplice Murderers receive a single-use Derringer. A miss or a hit on anyone else causes no kill and consumes it. If it shoots you dead, your current faction loses.

M4A【Saint】If a passenger or a meeting vote executes you, your current faction loses.

M1B【Marionette】Belongs to the murderer faction. You believe you are a passenger role not otherwise in play, but you are actually drunk.

M2B【Heretic】Belongs to the passenger faction. Swaps the passenger and murderer victory results, even after your death.

M3B【Goon】Anyone about to target you with an ability becomes drunk for one minute, and you join their faction. Cooldown: one minute. During that cooldown, incoming ability users become drunk for only one second.

M4B【Hermit】If you survive to the end, you win; if you do not, you lose.

M1C【Black Death】Belongs to the murderer faction. Starts in patient form. When killed by any means, becomes a pathogen possessing the killer, who becomes the new patient. If that patient is killed, their killer becomes infected. The pathogen uses the murderer Walkie-Talkie channel. Black Death ends only if the patient jumps off the train or dies from mental breakdown; the other murderers then die in burial sacrifice. If Black Death persists until only three players remain, the murderers win. Your faction cannot change.

M2C【Lunatic】You believe you are a Principal Murderer not otherwise in play. True Principal Murderers glow green to you; murderers see you in your role color. You are not drunk, but spending madness also deducts it from Principal Murderers, even below zero.

M3C【Ex Traitor】Belongs to the murderer faction. Otherwise has the abilities of a random Civilian Passenger not in play.

M4C【Amnesiac】Belongs to the passenger faction. Once per game, &lt;Inherit&gt; a body to become its owner's role, including items, and faction.

### Solo Neutrals

S1A【Arsonist】Starts with a [Master Key]. Instinct reveals all doused players. &lt;Douse&gt; a nearby player with gasoline. Cooldown: 10 seconds; the notification is delayed. Win when all living passengers have been doused.

S2A【Thief】Starts with a [Master Key]. Instinct reveals bodies. &lt;Scavenge&gt; a body, removing it and briefly revealing all players through walls for 10 seconds. Cooldown: 10 seconds. Win after scavenging bodies equal to one third of the player count.

S3A【Astral】&lt;Dream&gt; while your body stays still and your spirit explores, seeing normal skins. Cooldown after waking: 30 seconds. You continually know two living passenger roles and one living murderer role. Waking voluntarily beside one of them can kill them with magic. Win after three magic kills.

S1B【Jester】When executed, enters psychosis mode with shields equal to the number of previous wrongful kills plus one. Win by killing the shooter before psychosis ends. Otherwise, die by curtain call.

S2B【Executioner】&lt;Vendetta&gt; a nearby player. After a short delay, they learn that they have been framed. Have them executed, then choose another target. Win after two targets have been executed.

S3B【Kidnapper】&lt;Kidnap&gt; a nearby player. Cooldown: one minute. They possess you until their sanity reaches zero. Your death releases everyone still alive inside. Win after digesting three players.

S1C【Novelist】&lt;Deduce&gt; the role of any living or dead player. A correct guess is announced to everyone; an incorrect guess kills you. Win by correctly guessing the roles of one third of the players.

S2C【Angel】Has three shields. Once per game, &lt;Guard&gt; a nearby player; all damage they would take is redirected to you. Win by dying because of this protection.

S3C【Cold Wave】While you live, non-passengers also lose sanity. &lt;Freeze&gt; a nearby player after a delay. Cooldown: one minute. They become a cold source for one minute and glow to everyone. Anyone within 20 blocks loses sanity three times as fast. If they die during this time, their body becomes a permanent cold source. Win when three players die from mental breakdown while you are alive.

### Outsider Neutrals

O1A【Messiah】Belongs to the Cult faction. &lt;Preach&gt; to any player by guessing their role. Cooldown: one minute. A correct guess turns them into a follower and recruits them to the Cult; an incorrect guess exposes your role. Cult members follow neutral rules for picking up guns and performing executions, and can see each other through instinct. When Messiah dies, followers die by martyrdom.

O2A【Gardener】Belongs to the Gardener faction. &lt;Plant&gt; flowers on the train. Cooldown: 30 seconds; flowers must be at least 20 blocks apart. Flowers have three stages: seed for 30 seconds, invisible to others; seedling for one minute, during which they can be uprooted; and mature flower, able to move and attack players, but killed by one hit.

O1B【Majo】Belongs to the Coven faction. Starts with a [Gun] with a one-minute cooldown. Firing it is not an execution, so you do not drop it. Starts with one shield; whoever breaks it becomes a follower and joins your faction. Members can see each other through instinct. Can trigger the Majo epilogue.

O2B【Voodoo】Belongs to the Remnant faction. While you live, Enforcers and murderers become remnants in your faction after death. A remnant who jumps off the train becomes a normal spectator. Remnants share the Remnant voice channel and cannot hear other living players. Voodoo can see remnants through instinct and &lt;Resurrect&gt; one temporarily as their former role. After 30 seconds, they disappear and die permanently as a member of the Remnant faction.

O1C【Werewolf】Belongs to the Werewolf faction. At the start, randomly recruit total player count // 3 other players, retaining their roles and abilities. Recruits see each other through instinct. You cannot use instinct or be seen by their Werewolf instinct. When you die, they return to their previous factions.

O2C【Legion】Belongs to the Legion faction. Starts with a [Poison Needle]. Players poisoned by you do not die; instead, they join your faction. When the Legion member holding the needle dies, a random member becomes Legion and receives the needle.

## Traits

### Relationship Traits

**Lovers:** Randomly assigned. You know your partner's name. When one dies, the other dies for love. If both survive until the game ends, both win with the “Lovers Survived” announcement.

**Archenemies:** Pairs one Civilian Passenger with one Accomplice Murderer. Both know each other's name and role. If the passenger is executed, the murderer wins alone with the “Archenemy's Revenge” announcement.

**Twins:** Randomly assigned. You know your twin's name and role. Twins always share a faction. At initial faction assignment and whenever either changes faction, this priority determines whether they keep their faction or both change: Outsider > Murderer > Passenger > Solo Neutral.

**Nemesis:** Pairs one passenger with one non-passenger. Both know each other's name. If the non-passenger dies first, the passenger becomes Majo. If the passenger dies first, the non-passenger becomes Jester.

### General Traits

**Tease:** On death, broadcasts a fake epilogue title and music belonging to an outsider not in play, for up to two minutes.

**Reserved:** Walking, eating, drinking and similar actions make no sound.

**Trust:** Five seconds after the round begins, a member of your faction learns that you share their faction. If there is none, someone from another faction receives this false information instead.

**Crush:** When you die, one passenger becomes permanently drunk.

**Afterglow:** When death is triggered, enter a five-second dying state.

**Lucky:** Whenever you take damage, you have a 50% chance to survive.

**Familiar:** Item cooldowns are halved.

**Excellent:** Ability cooldowns are halved.

**Sleight:** Your held item is hidden.

**Energetic:** You can jump.

**Childlike:** Your body becomes smaller.

**Light:** Permanent slow falling.

**Night Owl:** Permanent night vision.

**Impatient:** Movement speed is multiplied by 1.1.

**Spirit Sense:** You are notified whenever someone dies.

**Obstinate:** Your death is announced to everyone.

**Resonant:** If you survive until the epilogue, everyone glows during it.

**Allergic:** You are notified whenever someone uses a remote ability on you.

### Passenger Traits

**Talkative:** Sanity does not decrease while you speak.

**Hoarding:** You can take two portions of food or drink.

**Justice:** You always die by suicide after a wrongful kill.

**Chaste:** Anyone who wrongfully kills you always dies by suicide.

**Gluttonous:** You finish food and drinks instantly.

**Hollow:** Reaching zero sanity does not kill you.

**Cleanly:** You can recognize poisoned plates and beds.

**Anxious:** Needs are generated 1.5 times as fast.

**Endurance:** Unlimited stamina.

**Optimist:** Unlimited sanity.

### Murderer Traits

**Stealth:** Entering psychosis mode does not play music.

**Playful:** Killing a player who is speaking grants an extra 100 madness.

**Closure:** Players you kill with melee weapons leave no body.

**Reckless:** You can pick up guns and bows, but hitting someone with one always makes you die by suicide.

**Premeditated:** If you can see other murderers through instinct, they appear in their respective role colors.

**Slacker:** Completing a fake task grants 25 madness.

**Greedy:** When a teammate dies, you inherit their madness.

**Anatomy:** You can examine bodies.

### Solo Traits

**Afterlife:** Start with one shield. When someone breaks it, you join their faction.

**Disguised:** Anyone about to use an ability on you becomes drunk for one second.

**Catcher:** You can die only by execution.

**Uprising:** You have a knife.

### Outsider Traits

**Last Wish:** Whoever kills you joins your faction.

**Bond:** One passenger starts as a member of your faction.

**Insight:** You have green instinct vision.

**Profiling:** Looking at a Solo Neutral reveals their role.
