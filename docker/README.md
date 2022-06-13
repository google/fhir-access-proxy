# Docker Compose YAMLs

This directory contains two Docker Compose YAML files.
[hapi-proxy-compose.yaml](./hapi-proxy-compose.yaml) sets up the FHIR Proxy and
a HAPI FHIR Server with synthetic data pre-loaded (more details below).
[keycloak/config-compose.yaml](./keycloak/config-compose.yaml) sets up a test
Keycloak instance that can support both a list based access control and a
single-patient based SMART-on-FHIR app (in two separate realms).

## Pre-loaded HAPI Server

The
[us-docker.pkg.dev/fhir-proxy-build/stable/hapi-synthea:latest](https://console.cloud.google.com/gcr/images/fhir-sdk/global/synthetic-data)
image is based on the HAPI FHIR
[image](https://hub.docker.com/r/hapiproject/hapi) with the
`1K Sample Synthetic Patient Records, FHIR R4` dataset from
[Synthea](https://synthea.mitre.org/downloads) stored in the container itself.
To load this dataset into the HAPI FHIR image, do the following:

1. Run a local version of the HAPI FHIR server:

   ```
   docker run --rm -d  -p 8080:8080 --name hapi_fhir hapiproject/hapi:latest
   ```

2. Download the `1K Sample Synthetic Patient Records, FHIR R4` dataset:

   ```
   wget https://synthetichealth.github.io/synthea-sample-data/downloads/synthea_sample_data_fhir_r4_sep2019.zip \
     -O fhir.zip
   ```

3. Unzip the file, a directory named `fhir` should be created containig JSON
   files:

   ```
   unzip fhir.zip
   ```

4. Use the Synthetic Data Uploader from the
   [FHIR Analytics](https://github.com/GoogleCloudPlatform/openmrs-fhir-analytics/tree/master/synthea-hiv)
   repo to upload the files into the HAPI FHIR container
   `docker run -it --network=host \ -e SINK_TYPE="HAPI" \ -e FHIR_ENDPOINT=http://localhost:8080/fhir \ -e INPUT_DIR="/workspace/output/fhir" \ -e CORES="--cores 1" \ -v $(pwd)/fhir:/workspace/output/fhir \ us-docker.pkg.dev/cloud-build-fhir/fhir-analytics/synthea-uploader:latest`

5. As the uploader uses `POST` to upload the JSON files, the server will create
   the ID used to refer to resources. We would like to upload a patient list
   example, but to do so, we need to fetch the IDs from the server. To do so,
   run:

   ```
   curl http://localhost:8080/fhir/Patient?_elements=fullUrl
   ```

6. Choose two Patient IDs (the two picked here are 2522 and 2707), then run the
   following to upload the list into the server

   ```
   PATIENT_ID1=2522
   PATIENT_ID2=2707
   curl -X PUT -H "Content-Type: application/json" \
     "http://localhost:8080/fhir/List/patient-list-example" \
     -d '{
         "resourceType": "List",
         "id": "patient-list-example",
         "status": "current",
         "mode": "working",
         "entry": [
            {
               "item": {
               "reference": "Patient/'"${PATIENT_ID1}"'"
               }
            },
            {
               "item": {
               "reference": "Patient/'"${PATIENT_ID2}"'"
               }
            }
         ]
      }'
   ```

7. Commit the Docker container. This saves its state into a new image

   ```
    docker commit hapi_fhir us-docker.pkg.dev/fhir-proxy-build/stable/hapi-synthea:latest
   ```

8. Push the image
   ```
    docker push us-docker.pkg.dev/fhir-proxy-build/stable/hapi-synthea:latest
   ```
