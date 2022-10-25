use crate::{Hittable, Ray, Vec3};
use crate::aabb::AABB;
use crate::hittable::HitRecord;
use crate::material::Material;

pub struct Plane<M: Material> {
    pub x0: f64,
    pub x1: f64,
    pub y0: f64,
    pub y1: f64,
    pub z: f64,
    pub material: M
}

impl<M: Material> Plane<M> {
    pub fn new(x0: f64, x1: f64, y0: f64, y1: f64, z: f64, material: M) -> Self {
        Self { x0, x1, y0, y1, z, material }
    }

    fn in_bounds(&self, ray: &Vec3) -> bool {
        self.x0 <= ray.x && ray.x <= self.x1 && self.y0 <= ray.y && ray.y <= self.y1
    }
}

impl<M: Material> Hittable for Plane<M> {
    fn hit(&self, ray: &Ray, t_min: f64, t_max: f64) -> Option<HitRecord> {
        let t = (self.z - ray.origin.z) / ray.dir.z;
        if t < t_min || t > t_max {
            return None;
        }
        if !self.in_bounds(&ray.at(t)) {
            return None;
        }
        self.get_hit_record(ray, t)
    }

    fn bounding_box(&self) -> Option<AABB> {
        Some(AABB::new(
            Vec3::new(self.x0, self.y0, self.z - 0.0001),
            Vec3::new(self.x1, self.y1, self.z + 0.0001),
        ))
    }

    fn get_normal(&self, _point: &Vec3) -> Vec3 {
        Vec3::new(0.0, 0.0, 1.0)
    }

    fn get_coordinates(&self, point: &Vec3) -> (f64, f64) {
        let u = (point.x - self.x0) / (self.x1 - self.x0);
        let v = (point.y - self.y0) / (self.y1 - self.y0);
        (u, v)
    }

    fn get_material(&self) -> &dyn Material {
        &self.material
    }
}
