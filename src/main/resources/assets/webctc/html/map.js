const host = location.host
const protocol = location.protocol;

const RAIL_DATA_URL = `${protocol}//${host}/api/rails/`
const SIGNAL_DATA_URL = `${protocol}//${host}/api/signals/`
const FORMATION_DATA_URL = `${protocol}//${host}/api/formations/`
const WAYPOINT_DATA_URL = `${protocol}//${host}/api/waypoints/`

var pzInstance

async function updateRail(svg, ws = false) {
  return await Promise.resolve()
    .then(async () => {
      if (!ws) {
        let railGroup = document.getElementById("rails")

        await fetch(RAIL_DATA_URL)
          .then(res => res.json())
          .then(json => {
            let updateList = Array.from(document.querySelectorAll("[id^='rail,']"))

            json.forEach(railCore => {
              let pos = railCore["pos"];
              let id = "rail," + pos[0] + "," + pos[1] + "," + pos[2] + ","
              let isTrainOnRail = railCore["isTrainOnRail"]
              let isStraightRail = railCore["railMaps"].every(railMap => railMap["isNotActive"] === undefined)

              let group = document.getElementById(id)
              if (group != null && isStraightRail) {
                updateList = updateList.filter(n => n !== group)
              } else {
                let newGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g')
                newGroup.id = id
                newGroup.setAttribute('stroke-width', '1.5px');
                railCore["railMaps"].sort(railMap => railMap["isNotActive"] === true ? -1 : 1).forEach(railMap => {
                  let startRP = railMap["startRP"];
                  let endRP = railMap["endRP"];
                  if (startRP != null && endRP != null) {
                    let isNotActive = railMap["isNotActive"] === true;
                    let startPosX = startRP["posX"]
                    let startPosZ = startRP["posZ"]
                    let endPosX = endRP["posX"]
                    let endPosZ = endRP["posZ"]
                    let line = createLine(startPosX, startPosZ, endPosX, endPosZ)
                    if (!isStraightRail) {
                      if (isNotActive) {
                        line.setAttribute('stroke', 'gray')
                      } else {
                        line.removeAttribute('stroke')
                      }
                    }
                    newGroup.appendChild(line)
                  }
                });
                railGroup.appendChild(newGroup)
                if (group != null) {
                  group.remove()
                }
                group = newGroup
              }
              let color = isTrainOnRail ? 'red' : 'white'
              if (group.getAttribute('stroke') !== color) {
                group.setAttribute('stroke', color)
              }
            });

            updateList.forEach(n => n.remove());
          })
      }
    })
    .then(() => {
      fetch(SIGNAL_DATA_URL)
        .then(res => res.json())
        .then(json => {
          let signalGroup = document.getElementById("signals")
          let updateList = Array.from(document.querySelectorAll("[id^='signal,']"))

          json.forEach(signal => {
            let pos = signal["pos"]
            let rotation = signal["rotation"]
            let blockDirection = signal["blockDirection"]
            let cos = Math.cos((270 + rotation) * (Math.PI / 180))
            let sin = Math.sin((270 + rotation) * (Math.PI / 180))
            let signalLevel = signal["signalLevel"]
            let p = (blockDirection * 90 - rotation + 360) % 360
            let fixX = (45 < p && p < 135) ? 3 : ((225 < p && p < 315) ? -3 : 0);

            let posX = pos[0]
            let posZ = pos[2]
            let id = "signal," + pos[0] + "," + pos[2] + ","

            let circle = this.createSignalCircle(posX, pos[1], posZ, signalLevel, fixX)

            let group = document.getElementById(id)
            if (group == null) {
              group = document.createElementNS('http://www.w3.org/2000/svg', 'g')
              group.id = id

              let line = createLineFromComponent(
                posX + 1.5 * cos, posZ - 1.5 * sin,
                2.5 * cos, -2.5 * sin
              )
              line.setAttribute('stroke', 'lightgray');
              line.setAttribute('stroke-width', '0.5px');
              line.setAttribute('name', 'verticalLine');
              group.appendChild(line);

              let baseLine = createLineFromComponent(
                posX + 4 * cos + 1.5 * sin, posZ - 4 * sin + 1.5 * cos,
                -3.0 * sin, -3.0 * cos
              )
              baseLine.setAttribute('stroke', 'lightgray');
              baseLine.setAttribute('stroke-width', '0.5px');
              baseLine.setAttribute('name', 'horizontalLine');
              group.appendChild(baseLine);

              group.appendChild(circle)

              signalGroup.appendChild(group);
            } else {
              updateList = updateList.filter(n => n !== group)
              let minus = 0
              let last;
              let circleArray = Array.from(group.getElementsByTagName('circle'))
              let support
              if ((support = group.getElementsByTagName('g')[0]) == null) {
                support = document.createElementNS('http://www.w3.org/2000/svg', 'g');
                group.appendChild(support);
              }

              if (support.innerHTML !== "") {
                support.innerHTML = ""
              }

              let circleCache = circleArray.find(c => Number(c.getAttribute("yCoord")) === Number(pos[1]))
              if (circleCache == null) {
                circleArray.push(circle)
              } else {
                let color = getSignalColor(signalLevel)
                if (circleCache.getAttribute('fill') !== color) {
                  circleCache.setAttribute('fill', color)
                }
              }

              if (circleArray.length > 1) {
                circleArray
                  .sort((a, b) => Number(a.getAttribute("yCoord")) < Number(b.getAttribute("yCoord")) ? -1 : 1)
                  .forEach((value, index) => {
                    let fixX = Number(value.getAttribute("fix"))
                    if (fixX === 0 && index === 0) {
                      minus += (index - last) ? -1.5 : +1.5
                      last = index
                      fixX = Number(circleArray[index + 1].getAttribute("fix")) * -1
                    } else if (fixX !== 0 && index !== 0) {
                      minus += (index - last) ? -1.5 : +1.5
                      last = index
                      fixX = 0;
                    } else if (fixX !== 0) {
                      minus += (index - last) ? -1.5 : +1.5
                      last = index
                    } else if (index - last) {
                      minus -= 1.0
                    }
                    let cx = posX - (3.5 * index + minus) * cos - fixX * sin
                    let cy = posZ + (3.5 * index + minus) * sin - fixX * cos
                    value.setAttribute('cx', cx)
                    value.setAttribute('cy', cy)
                    group.appendChild(value)

                    if (fixX !== 0 || fixX === 0 && index === 0) {
                      let polyline = document.createElementNS('http://www.w3.org/2000/svg', 'polyline');
                      polyline.setAttribute('points',
                        (cx + 1.5 * cos) + "," + (cy - 1.5 * sin) + " " +
                        (cx + 3 * cos) + "," + (cy - 3 * sin) + " " +
                        (cx + 3 * cos + fixX * sin) + "," + (cy - 3 * sin + fixX * cos)
                      );
                      polyline.setAttribute('stroke', 'lightgray');
                      polyline.setAttribute('stroke-width', '0.5px');
                      polyline.setAttribute('name', 'holizonalLine');
                      polyline.setAttribute('fill', 'none');
                      support.appendChild(polyline);
                    }
                  })
              }
              Array.from(group.getElementsByTagName('line'))
                .filter(line => line.getAttribute("name") === "verticalLine")
                .find(line => {
                  line.setAttribute('x1', posX + (1.5 - 2.5 * circleArray.length - minus) * cos);
                  line.setAttribute('y1', posZ - (1.5 - 2.5 * circleArray.length - minus) * sin);
                })
            }
          });

          updateList.forEach(n => n.remove());
        })
      let formationGroup = document.getElementById("formations")
      if (formationGroup != null) {
        fetch(FORMATION_DATA_URL).then(res => res.json()).then(json => {
          let updateList = Array.from(document.querySelectorAll("[id^='formation,']"))
          let updateList2 = Array.from(document.querySelectorAll("[id^='formation-li,']"))
          json.forEach(formation => {
            if (formation != null && formation["controlCar"] != null) {
              let id = "formation," + formation["id"] + ","
              let group = document.getElementById(id)

              if (group == null) {
                group = document.createElementNS('http://www.w3.org/2000/svg', 'g')
                group.id = id
                formationGroup.appendChild(group)
              } else {
                updateList = updateList.filter(n => n !== group)
              }
              json = formation["controlCar"]
              group.innerHTML = ""
              let pos = json["pos"]
              let posX = pos[0]
              let posZ = pos[2]

              let rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
              rect.setAttribute('x', String(posX - 3))
              rect.setAttribute('y', String(posZ - 3))
              rect.setAttribute('width', "6px")
              rect.setAttribute('height', "6px")
              rect.setAttribute('fill', json["trainStateData"][4] === 0 ? "YELLOW" : "DARKGRAY")
              rect.setAttribute('stroke', "GRAY")
              group.appendChild(rect)

              let text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
              let textContent = Math.round(json["speed"] * 72) + "km/h " + (json["name"] === "no_name" ? "" : json["name"] + " ") + json["driver"]
              text.textContent = textContent
              text.setAttribute('x', String(posX + 5))
              text.setAttribute('y', String(posZ + 3))
              text.setAttribute('font-size', "8")
              text.setAttribute('font-weight', "bold")
              text.setAttribute('fill', "white")
              text.setAttribute('stroke-width', "0.35px")
              group.appendChild(text);


              let bbox = text.getBBox();
              let background = document.createElementNS("http://www.w3.org/2000/svg", "rect");
              background.setAttribute('x', String(bbox.x))
              background.setAttribute('y', String(bbox.y))
              background.setAttribute('width', String(bbox.width))
              background.setAttribute('height', String(bbox.height))
              background.setAttribute('fill', "#000000")
              background.setAttribute('fill-opacity', "0.5")
              background.setAttribute('rx', "1px")
              background.setAttribute('ry', "1px")
              group.insertBefore(background, text);

              if (trackingFormationId === formation["id"]) {
                console.log(pzInstance.getTransform());
                let scale = pzInstance.getTransform()["scale"];
                pzInstance.moveTo((-posX) * scale + window.innerWidth / 2, (-posZ) * scale + (window.innerHeight - 80) / 2)
              }

              let fli = updateFormationListItem(formation["id"], textContent)
              updateList2 = updateList2.filter(n => n !== fli)
            }
          });
          updateList.forEach(n => n.remove());
          updateList2.forEach(n => n.remove());
        }).catch(err => {
        })
      }

      let wayPointsGroup = document.getElementById("waypoints")
      if (wayPointsGroup != null && !ws) {
        fetch(WAYPOINT_DATA_URL).then(res => res.json()).then(json => {
          json.forEach(waypoint => {
            let group = document.createElementNS('http://www.w3.org/2000/svg', 'g')
            wayPointsGroup.appendChild(group)

            let title = document.createElementNS('http://www.w3.org/2000/svg', 'title')
            title.textContent = `ID: ${waypoint["identifyName"]}`
            group.appendChild(title)

            let text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            text.textContent = waypoint["displayName"]
            text.setAttribute('x', waypoint["pos"]["x"])
            text.setAttribute('y', waypoint["pos"]["z"])
            text.setAttribute('font-size', "8")
            text.setAttribute('font-weight', "bold")
            text.setAttribute('fill', "white")
            text.setAttribute('stroke-width', "0.35px")
            text.setAttribute('text-anchor', "middle")
            group.appendChild(text);


            let bbox = text.getBBox();
            let background = document.createElementNS("http://www.w3.org/2000/svg", "rect");
            background.setAttribute('x', String(bbox.x - 2))
            background.setAttribute('y', String(bbox.y))
            background.setAttribute('width', String(bbox.width + 4))
            background.setAttribute('height', String(bbox.height))
            background.setAttribute('fill', "#000000")
            background.setAttribute('fill-opacity', "0.8")
            background.setAttribute('rx', "2px")
            background.setAttribute('ry', "2px")
            group.insertBefore(background, text);
          })
        })
      }
    })
}

let trackingFormationId = -1;

function updateFormationListItem(fId, text) {
  let id = "formation-li," + fId + ","
  let li = document.getElementById(id)
  if (li == null) {
    li = document.createElement("li")
    li.id = id
    document.getElementById("formation-list").appendChild(li)
  }
  li.className = "list-group-item"
  li.innerText = text
  li.style.background = trackingFormationId === fId ? "LIGHTBLUE" : "WHITE"
  li.onclick = () => trackingFormationId = (trackingFormationId === fId ? -1 : fId)
  return li;
}

function updateRailState(svg, json) {
  json.forEach(railCore => {
    let pos = railCore["pos"];
    let id = "rail," + pos[0] + "," + pos[1] + "," + pos[2] + ","
    let isTrainOnRail = railCore["isTrainOnRail"]
    let isStraightRail = railCore["railMaps"].every(railMap => railMap["isNotActive"] === undefined)

    let group = document.getElementById(id)
    if (group != null && isStraightRail) {
    } else {
      let newGroup = document.createElementNS('http://www.w3.org/2000/svg', 'g')
      newGroup.id = id
      newGroup.setAttribute('stroke-width', '1.5px');
      railCore["railMaps"].sort(railMap => railMap["isNotActive"] === true ? -1 : 1).forEach(railMap => {
        let startRP = railMap["startRP"];
        let endRP = railMap["endRP"];
        if (startRP != null && endRP != null) {
          let isNotActive = railMap["isNotActive"] === true;
          let startPosX = startRP["posX"]
          let startPosZ = startRP["posZ"]
          let endPosX = endRP["posX"]
          let endPosZ = endRP["posZ"]
          let line = createLine(startPosX, startPosZ, endPosX, endPosZ)
          if (!isStraightRail) {
            if (isNotActive) {
              line.setAttribute('stroke', 'gray')
            } else {
              line.removeAttribute('stroke')
            }
          }
          newGroup.appendChild(line)
        }
      });
      document.getElementById("rails").appendChild(newGroup)
      if (group != null) {
        group.remove()
      }
      group = newGroup
    }
    let color = isTrainOnRail ? 'red' : 'white'
    if (group.getAttribute('stroke') !== color) {
      group.setAttribute('stroke', color)
    }
  });
}

function getSignalColor(signalLevel) {
  switch (signalLevel) {
    case 0:
      return "darkslategray"
    case 1:
      return "rgb(2045,0,0)"
    case 2:
      return "rgb(255,153,0)"
    case 3:
      return "rgb(255,204,0)"
    case 4:
      return "rgb(155,255,0)"
    case 5:
      return "rgb(51,204,0)"
    default:
      return "rgb(51,102,255)"
  }
}

function createSignalCircle(posX, yCoord, posZ, signalLevel, fixX) {
  let circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
  circle.setAttribute('yCoord', yCoord)
  circle.setAttribute('fix', fixX)
  circle.setAttribute('cx', posX)
  circle.setAttribute('cy', posZ)
  circle.setAttribute('r', "1.5px")
  circle.setAttribute('fill', getSignalColor(signalLevel))
  circle.setAttribute('stroke', "lightgray")
  circle.setAttribute('stroke-width', "0.5px")
  return circle
}

function createLineFromComponent(x, y, xComponent, yComponent) {
  return createLine(x, y, x + xComponent, y + yComponent)
}

function createLine(x1, y1, x2, y2) {
  let line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
  line.setAttribute('x1', String(x1));
  line.setAttribute('y1', String(y1));
  line.setAttribute('x2', String(x2));
  line.setAttribute('y2', String(y2));
  return line
}