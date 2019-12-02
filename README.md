# TrainCartsDestinationSelector
Simple destination selector sign for the [traincarts](https://www.spigotmc.org/resources/traincarts.39592/) plugin.

## Usage
Usage ~is~ should be simple and intuative.
1. First choose a spot where you want the selector to be:
   ![Find a Spot](https://raw.githubusercontent.com/7kasper/TrainCartsDestinationSelector/master/resources/01-spot.png)
2. Place a sign with just the line `[TCDS]` down.
   ![Place TCDS Sign](https://raw.githubusercontent.com/7kasper/TrainCartsDestinationSelector/master/resources/02-selector.png)
3. Order doesn't really matter. In this case no destination lists are present yet so this will be displayed:
   ![Selector Result](https://raw.githubusercontent.com/7kasper/TrainCartsDestinationSelector/master/resources/03-selectorresult.png)
4. After a while the timeout message will appear. The behaviour of this is configurable in the config.
   ![Selector Timeout](https://raw.githubusercontent.com/7kasper/TrainCartsDestinationSelector/master/resources/04-selectorthing.png)
   _NOTE: If you want to disable the timeout message you can. Please do note that the train's destination is only set upon use. Disabling some kind of message might thus confuse your players.
5. Now destinations can be added to the selector by creating destination lists.
   This is done by creating a sign with `[TCDS]` on the first line. The other lines must contain one or more destination. Every line should be a specific destination applicable in `/train destination <destination>`.
   ![Destination List](https://raw.githubusercontent.com/7kasper/TrainCartsDestinationSelector/master/resources/05-destonationlist.png)
6. More destinations can be added. See the place of the sign. Destination lists only work when attached to a block behind the destination selector, or a block behind a block where another destionation list is attached. Both the left and the right side work. Destinations are added in order of left to right, front to back.
   ![More Destinations](https://raw.githubusercontent.com/7kasper/TrainCartsDestinationSelector/master/resources/06-moredestination.png)
7. The destination selector now works! Use `right-click` to cycle down and `left-click` to cycle up. The sign essentially runs `/train destionation <selected>` on the player using the sign. This means all the rules and permissions of this command apply. The sign works best when used from inside a train.
   ![Profit](https://raw.githubusercontent.com/7kasper/TrainCartsDestinationSelector/master/resources/07-profit.png)
   
## Permissions
`traincartsdestinationselector.*` - Grants permission to both use and make TCDS signs.  
`traincartsdestinationselector.use` - Grants permission to use a destination selector.  
`traincartsdestinationselector.make` - Grants permission to make both destination selector and destination list signs.
   
## Config
```YAML
# == TrainCartsDestinationSelector ==
#     Some basic options for the    #
#     destination selector sign     #
# == TrainCartsDestinationSelector ==

# Timeout in seconds before the rest message is displayed.
# Set to -1 to disable the timeout message.
timeout: 5
# Reset selection progress when timeout is displayed?
reset: true
# Timeout message:
message:
  - '[&6TCDS&0]'
  - 'Click to'
  - 'set train'
  - 'destination'
```
