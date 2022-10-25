#[cfg(test)]
mod tests {
    #[test]
    fn test_vec3_add() {
        let vec1 = Vec3::new(1.0, 5.0, 7.0);
        let vec2 = Vec3::new(-5.0, 10.0, 0.0);
        assert_eq!(Vec3::new(-4.0, 15.0, 7.0), vec1 + vec2);
    }

    #[test]
    fn test_vec3_sub() {
        let vec1 = Vec3::new(1.0, 5.0, 7.0);
        let vec2 = Vec3::new(-5.0, 10.0, 0.0);
        assert_eq!(Vec3::new(6.0, -5.0, 7.0), vec1 - vec2);
    }

    #[test]
    fn test_vec3_mul() {
        let vec1 = Vec3::new(1.0, 5.0, 7.0);
        let vec2 = Vec3::new(-5.0, 10.0, 0.0);
        assert_eq!(Vec3::new(-5.0, 50.0, 0.0), vec1 * vec2);
    }

    #[test]
    fn test_vec3_mul_f64() {
        let vec1 = Vec3::new(1.0, 5.0, 7.0);
        let f64 = 2.0;
        assert_eq!(Vec3::new(2.0, 10.0, 14.0), vec1 * f64);
    }

    #[test]
    fn test_vec3_mul_f64_reverse() {
        let vec1 = Vec3::new(1.0, 5.0, 7.0);
        let f64 = 2.0;
        assert_eq!(Vec3::new(2.0, 10.0, 14.0), f64 * vec1);
    }

    #[test]
    fn test_vec3_dot() {
        let vec1 = Vec3::new(1.0, 5.0, 7.0);
        let vec2 = Vec3::new(-5.0, 10.0, 0.0);
        assert_eq!(-45.0, dot(&vec1, &vec2));
    }

    #[test]
    fn test_vec3_cross() {
        let vec1 = Vec3::new(1.0, 5.0, 7.0);
        let vec2 = Vec3::new(-5.0, 10.0, 0.0);
        assert_eq!(Vec3::new(50.0, -5.0, -50.0), cross(&vec1, &vec2));
    }

    #[test]
    fn test_vec3_length() {
        let vec1 = Vec3::new(1.0, 5.0, 7.0);
        assert_eq!(9.055385138137417, vec1.length());
    }

    #[test]
    fn test_vec3_unit() {
        let vec1 = Vec3::new(1.0, 5.0, 7.0);
        assert_eq!(Vec3::new(0.1091089451179964, 0.545544725589982, 0.7707723627849813), vec1.unit());
    }

    #[test]
    fn test_vec3_index() {
        let vec1 = Vec3::new(1.0, 5.0, 7.0);
        assert_eq!(1.0, vec1[0]);
        assert_eq!(5.0, vec1[1]);
        assert_eq!(7.0, vec1[2]);
    }

    #[test]
    #[should_panic]
    fn test_vec3_index_out_of_bounds() {
        let vec1 = Vec3::new(1.0, 5.0, 7.0);
        let _ = vec1[3];
    }

    #[test]
    fn test_vec3_index_mut() {
        let mut vec1 = Vec3::new(1.0, 5.0, 7.0);
        vec1[0] = 2.0;
        vec1[1] = 6.0;
        vec1[2] = 8.0;
        assert_eq!(2.0, vec1[0]);
        assert_eq!(6.0, vec1[1]);
        assert_eq!(8.0, vec1[2]);
    }

    #[test]
    #[should_panic]
    fn test_vec3_index_mut_out_of_bounds() {
        let mut vec1 = Vec3::new(1.0, 5.0, 7.0);
        vec1[3] = 2.0;
    }

    #[test]
    fn test_vec3_from_array() {
        let array = [1.0, 5.0, 7.0];
        let vec1 = Vec3::from(array);
        assert_eq!(1.0, vec1[0]);
        assert_eq!(5.0, vec1[1]);
        assert_eq!(7.0, vec1[2]);
    }

    #[test]
    fn test_vec3_from_slice() {
        let slice = &[1.0, 5.0, 7.0];
        let vec1 = Vec3::from(slice);
        assert_eq!(1.0, vec1[0]);
        assert_eq!(5.0, vec1[1]);
        assert_eq!(7.0, vec1[2]);
    }

    #[test]
    fn test_vec3_from_vec() {
        let vec = vec![1.0, 5.0, 7.0];
        let vec1 = Vec3::from(vec);
        assert_eq!(1.0, vec1[0]);
        assert_eq!(5.0, vec1[1]);
        assert_eq!(7.0, vec1[2]);
    }
}