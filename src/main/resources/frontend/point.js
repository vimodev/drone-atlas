const exclude_attributes = ['video', 'id', 'videoId', 'created_at', 'updated_at']
const video_exclude_attributes = ['id', 'fileId', 'file', 'created_at', 'updated_at']
const file_exclude_attributes = ['id', 'created_at', 'updated_at']

// Get the point id from the url params
function getPointId() {
  return (new URLSearchParams(window.location.search)).get("point")
}

// Fetch the point from the backend
async function fetchPointInformation(pointId) {
  return await new Promise((resolve) => {
    fetch("/api/point?id=" + pointId + "&recurse=true")
      .then(function (response) {
        return response.json()
      })
      .then(function (info) {
        return resolve(info)
      })
  })
}

// Append a key and a value to the info table
function appendKeyValue(key, value) {
  const tab = document.getElementById("table")
  const row = document.createElement("tr")
  const keycell = document.createElement("td")
  keycell.innerHTML = key
  keycell.style.textAlign = "center"
  const valuecell = document.createElement("td")
  valuecell.innerHTML = value
  valuecell.style.textAlign = "center"
  row.appendChild(keycell)
  row.appendChild(valuecell)
  tab.appendChild(row)
}

// Populate the page with the information in the point
function populate(info) {
  appendKeyValue('<b>Datapoint Info:</b>', '<a id="videoLink">Play video</a>')
  for (const key in info) {
    if (info.hasOwnProperty(key) && exclude_attributes.indexOf(key) == -1) {
      appendKeyValue(key, info[key])
    }
  }
  appendKeyValue('<b>Video Info:</b>', '')
  for (const key in info.video) {
    if (info.video.hasOwnProperty(key) && video_exclude_attributes.indexOf(key) == -1) {
      appendKeyValue(key, info.video[key])
    }
  }
  for (const key in info.video.file) {
    if (info.video.file.hasOwnProperty(key) && file_exclude_attributes.indexOf(key) == -1) {
      appendKeyValue(key, info.video.file[key])
    }
  }
  const link = document.getElementById("videoLink")
  link.onclick = () => {
    fetch("/api/file/open?id=" + info.video.file.id)
  }
  document.body.style.backgroundImage = "url('" + "/api/video/thumbnail?id=" + info.video.id + "&t=" + info.startSeconds + "')"
}

// Run functions in order
const point_id = getPointId()
const point = fetchPointInformation(point_id)
point.then(populate)
