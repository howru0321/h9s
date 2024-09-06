package utils

var PodList = make(map[string]bool)

func GetTrueElements() []string {
	var result []string
	for key, value := range PodList {
		if value {
			result = append(result, key) // Collect the keys with true values
		}
	}
	return result
}